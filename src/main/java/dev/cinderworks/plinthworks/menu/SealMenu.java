package dev.cinderworks.plinthworks.menu;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.block.PlinthBlock;
import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.component.*;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.seal.SealMatch;
import dev.cinderworks.plinthworks.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;

import java.util.*;

// ghost-slot filter editor for one seal (ITEM / ITEM_EXACT / MOD): drop to add a key, click to remove
public class SealMenu extends AbstractContainerMenu
{
	public static final int GHOST = 9;
	public static final int TOGGLE = 0;
	public static final int CLEAR = 1;

	private final PlinthBlockEntity be;
	private final int sealIndex;
	private final SealType type;
	private final ItemStackHandler ghost = new ItemStackHandler(GHOST);
	private final List<MatchKey> keys = new ArrayList<>();
	private final ContainerData mode;
	private boolean whitelist = true;

	public SealMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf data) {
		this(id, playerInv, (PlinthBlockEntity) playerInv.player.level().getBlockEntity(data.readBlockPos()),
				data.readVarInt());
	}

	public SealMenu(int id, Inventory playerInv, PlinthBlockEntity be, int sealIndex) {
		super(ModMenus.SEAL.get(), id);
		this.be = be;
		this.sealIndex = sealIndex;
		ItemStack sealStack = be.seals().getStackInSlot(sealIndex);
		this.type = sealStack.getItem() instanceof SealItem seal ? seal.type() : SealType.ITEM;
		seed(sealStack);
		this.mode = new ContainerData() {
			public int get(int i) { return whitelist ? 1 : 0; }
			public void set(int i, int v) { whitelist = v != 0; }
			public int getCount() { return 1; }
		};
		for (int i = 0; i < GHOST; i++) {
			addSlot(new GhostSlot(ghost, i, 8 + i * 18, 20));
		}
		addPlayerSlots(playerInv);
		addDataSlots(mode);
	}

	private void seed(ItemStack sealStack) {
		SealConfig config = sealStack.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY);
		whitelist = config.whitelist();
		for (MatchKey key : config.keys()) {
			if (keys.size() >= GHOST) break;
			ghost.setStackInSlot(keys.size(), representative(key));
			keys.add(key);
		}
	}

	private void addPlayerSlots(Inventory inventory) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, 142));
		}
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
		if (slotId < 0 || slotId >= GHOST) {
			super.clicked(slotId, dragType, clickType, player);
			return;
		}
		if (clickType == ClickType.THROW) {
			return;
		}
		ItemStack held = getCarried();
		if (clickType == ClickType.CLONE) {
			if (player.isCreative() && held.isEmpty() && slotId < keys.size()) {
				ItemStack copy = ghost.getStackInSlot(slotId).copy();
				copy.setCount(copy.getMaxStackSize());
				setCarried(copy);
			}
			return;
		}
		if (held.isEmpty()) {
			removeAt(slotId);
		} else {
			addKey(keyFor(held), held);
		}
		save();
		broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index >= GHOST) {
			ItemStack stack = slots.get(index).getItem();
			if (!stack.isEmpty()) {
				addKey(keyFor(stack), stack);
			}
		} else {
			removeAt(index);
		}
		save();
		broadcastChanges();
		return ItemStack.EMPTY;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
		return false;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == TOGGLE) {
			whitelist = !whitelist;
		} else if (id == CLEAR) {
			keys.clear();
			for (int i = 0; i < GHOST; i++) {
				ghost.setStackInSlot(i, ItemStack.EMPTY);
			}
		} else {
			return false;
		}
		save();
		broadcastChanges();
		return true;
	}

	private void addKey(MatchKey key, ItemStack source) {
		if (key == null || keys.size() >= GHOST || keys.contains(key)) {
			return;
		}
		ghost.setStackInSlot(keys.size(), source.copyWithCount(1));
		keys.add(key);
	}

	private void removeAt(int i) {
		if (i >= keys.size()) {
			return;
		}
		keys.remove(i);
		for (int j = i; j < GHOST - 1; j++) {
			ghost.setStackInSlot(j, ghost.getStackInSlot(j + 1));
		}
		ghost.setStackInSlot(GHOST - 1, ItemStack.EMPTY);
	}

	private MatchKey keyFor(ItemStack stack) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return switch (type) {
			case ITEM -> new MatchKey.ItemKey(itemId);
			case ITEM_EXACT -> new MatchKey.ComponentKey(Plinthworks.id("exact"), SealMatch.componentFingerprint(stack));
			case MOD -> new MatchKey.ModKey(itemId.getNamespace());
			default -> null;
		};
	}

	// rebuild the shown item for a stored key so reopening a seal still displays its filters
	private static ItemStack representative(MatchKey key) {
		return switch (key) {
			case MatchKey.ItemKey item -> stackOf(item.item());
			case MatchKey.ModKey mod -> firstOf(mod.namespace());
			case MatchKey.ComponentKey exact -> stackOf(parseItem(exact.value()));
			default -> ItemStack.EMPTY;
		};
	}

	private static ItemStack stackOf(ResourceLocation id) {
		return id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
	}

	private static ResourceLocation parseItem(String fingerprint) {
		int cut = fingerprint.indexOf('|');
		return ResourceLocation.tryParse(cut < 0 ? fingerprint : fingerprint.substring(0, cut));
	}

	private static ItemStack firstOf(String namespace) {
		return BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals(namespace))
				.findFirst().map(SealMenu::stackOf).orElse(ItemStack.EMPTY);
	}

	private void save() {
		ItemStack sealStack = be.seals().getStackInSlot(sealIndex);
		if (!(sealStack.getItem() instanceof SealItem)) {
			return;
		}
		sealStack = sealStack.copy();
		sealStack.set(ModDataComponents.SEAL_CONFIG.get(), new SealConfig(whitelist, keys));
		be.seals().setStackInSlot(sealIndex, sealStack);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		// return to the plinth gui the seal was opened from - next tick, so we aren't reopening mid-close
		if (!(player instanceof ServerPlayer sp)) {
			return;
		}
		BlockPos pos = be.getBlockPos();
		if (sp.getServer() == null) {
			return;
		}
		sp.getServer().execute(() -> {
			if (sp.isRemoved() || sp.containerMenu != sp.inventoryMenu) {
				return;
			}
			if (sp.level().getBlockEntity(pos) instanceof PlinthBlockEntity live) {
				PlinthBlock.openMenu(sp, live, pos);
			}
		});
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(be, player);
	}

	public boolean whitelist() {
		return mode.get(0) != 0;
	}

	public int keyCount() {
		int count = 0;
		for (int i = 0; i < GHOST; i++) {
			if (!ghost.getStackInSlot(i).isEmpty()) count++;
		}
		return count;
	}
}
