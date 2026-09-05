package dev.hartforge.plinthworks.menu;

import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.component.*;
import dev.hartforge.plinthworks.item.*;
import dev.hartforge.plinthworks.logic.UpgradeSet;
import dev.hartforge.plinthworks.logic.seal.SealMatch;
import dev.hartforge.plinthworks.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PlinthMenu extends AbstractContainerMenu
{
	private final PlinthBlockEntity be;
	private final ContainerData stats;
	private final int plinthSlots;
	private final java.util.List<BlockPos> networkMembers;
	private final StringBuilder channelInput = new StringBuilder();

	public PlinthMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf data) {
		this(id, playerInv, data.readBlockPos(), readMembers(data));
	}

	public PlinthMenu(int id, Inventory playerInv, BlockPos pos) {
		this(id, playerInv, pos, java.util.List.of());
	}

	private PlinthMenu(int id, Inventory playerInv, BlockPos pos, java.util.List<BlockPos> members) {
		this(id, playerInv, (PlinthBlockEntity) playerInv.player.level().getBlockEntity(pos),
				new SimpleContainerData(7), members);
	}

	public PlinthMenu(int id, Inventory playerInv, PlinthBlockEntity be) {
		this(id, playerInv, be, serverStats(be), be.networkMembers());
	}

	private PlinthMenu(int id, Inventory playerInv, PlinthBlockEntity be, ContainerData stats,
			java.util.List<BlockPos> networkMembers) {
		super(ModMenus.PLINTH.get(), id);
		this.be = be;
		this.stats = stats;
		this.networkMembers = java.util.List.copyOf(networkMembers);
		this.plinthSlots = 2 + be.etchings().getSlots() + be.seals().getSlots();
		addSlot(new SigilSlot(be.sigil(), 0, 17, 29));
		for (int i = 0; i < be.etchings().getSlots(); i++) {
			addSlot(new EtchingSlot(be.etchings(), i, 53 + i * 18, 29));
		}
		for (int i = 0; i < be.seals().getSlots(); i++) {
			addSlot(new SealSlot(be.seals(), i, 71 + i * 18, 57));
		}
		addSlot(new SlotItemHandler(be.display(), 0, 17, 57));
		addPlayerSlots(playerInv);
		addDataSlots(stats);
	}

	private void addPlayerSlots(Inventory inventory) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 137 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, 195));
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return result;
		}
		ItemStack stack = slot.getItem();
		result = stack.copy();
		int etchingEnd = 1 + be.etchings().getSlots();
		int sealEnd = etchingEnd + be.seals().getSlots();
		if (index < plinthSlots) {
			if (!moveItemStackTo(stack, plinthSlots, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.getItem() instanceof SigilItem) {
			if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
		} else if (stack.getItem() instanceof EtchingItem) {
			if (!moveItemStackTo(stack, 1, etchingEnd, false)) return ItemStack.EMPTY;
		} else if (stack.getItem() instanceof SealItem) {
			if (!moveItemStackTo(stack, etchingEnd, sealEnd, false)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, sealEnd, plinthSlots, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return result;
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(be, player);
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == 30) {
			channelInput.setLength(0);
			return true;
		}
		if (id >= 1000 && id <= 1127 && channelInput.length() < 32) {
			channelInput.append((char) (id - 1000));
			return true;
		}
		if (id == 31 && be.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			String name = LinkToolItem.channelName(channelInput.toString());
			dev.hartforge.plinthworks.logic.network.PlinthNetwork.get(serverLevel).add(name, be.getBlockPos());
			be.setChannel(name);
			broadcastChanges();
			return true;
		}
		if (id == 20 && be.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& !be.channel().isEmpty()) {
			dev.hartforge.plinthworks.logic.network.PlinthNetwork.get(serverLevel).cycleMode(be.channel());
			broadcastChanges();
			return true;
		}
		ItemStack sealStack = firstSeal();
		if (!(sealStack.getItem() instanceof SealItem seal)) {
			return false;
		}
		sealStack = sealStack.copy();
		SealConfig config = sealStack.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY);
		if (id == 0) {
			sealStack.set(ModDataComponents.SEAL_CONFIG.get(), config.toggleMode());
			be.seals().setStackInSlot(firstSealIndex(), sealStack);
			broadcastChanges();
			return true;
		}
		if (id == 1) {
			sealStack.set(ModDataComponents.SEAL_CONFIG.get(), new SealConfig(config.whitelist(), java.util.List.of()));
			be.seals().setStackInSlot(firstSealIndex(), sealStack);
			broadcastChanges();
			return true;
		}
		MatchKey key = keyFor(seal.type(), id);
		if (key == null) {
			return false;
		}
		sealStack.set(ModDataComponents.SEAL_CONFIG.get(), config.with(key));
		be.seals().setStackInSlot(firstSealIndex(), sealStack);
		broadcastChanges();
		return true;
	}

	public PlinthBlockEntity blockEntity() {
		return be;
	}

	public int verb() {
		return stats.get(0);
	}

	public int intervalTicks() {
		return stats.get(1);
	}

	public int throughput() {
		return stats.get(2);
	}

	public int bufferSlots() {
		return stats.get(3);
	}

	public int range() {
		return stats.get(4);
	}

	public int channelMembers() {
		return stats.get(5);
	}

	public int transferMode() {
		return stats.get(6);
	}

	public java.util.List<BlockPos> networkMembers() {
		return networkMembers;
	}

	public ItemStack firstSeal() {
		for (int i = 0; i < be.seals().getSlots(); i++) {
			ItemStack stack = be.seals().getStackInSlot(i);
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private int firstSealIndex() {
		for (int i = 0; i < be.seals().getSlots(); i++) {
			if (!be.seals().getStackInSlot(i).isEmpty()) {
				return i;
			}
		}
		return 0;
	}

	private MatchKey keyFor(SealType type, int id) {
		ItemStack shown = be.getDisplayedItem();
		if (shown.isEmpty()) {
			return null;
		}
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(shown.getItem());
		return switch (type) {
			case ITEM -> new MatchKey.ItemKey(itemId);
			case ITEM_EXACT -> new MatchKey.ComponentKey(Plinthworks.id("exact"),
					SealMatch.componentFingerprint(shown));
			case MOD -> new MatchKey.ModKey(itemId.getNamespace());
			case TAG -> shown.getTags().skip(Math.max(0, id - 100L)).findFirst()
					.map(tag -> (MatchKey) new MatchKey.TagKey(tag.location())).orElse(null);
			case COMPONENT -> shown.getComponents().stream().skip(Math.max(0, id - 200L)).findFirst()
					.map(component -> componentKey(component)).orElse(null);
		};
	}

	private static MatchKey componentKey(TypedDataComponent<?> component) {
		return new MatchKey.ComponentKey(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type()),
				String.valueOf(component.value()));
	}

	private static java.util.List<BlockPos> readMembers(RegistryFriendlyByteBuf data) {
		int size = data.readVarInt();
		java.util.ArrayList<BlockPos> members = new java.util.ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			members.add(data.readBlockPos());
		}
		return members;
	}

	private static ContainerData serverStats(PlinthBlockEntity be) {
		return new ContainerData() {
			@Override
			public int get(int index) {
				UpgradeSet u = be.upgrades();
				return switch (index) {
					case 0 -> u.verb() == null ? 0 : u.verb().ordinal() + 1;
					case 1 -> u.intervalTicks();
					case 2 -> u.throughput();
					case 3 -> u.bufferSlots();
					case 4 -> u.range();
					case 5 -> be.channelMembers();
					case 6 -> {
						if (be.channel().isEmpty() || !(be.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) {
							yield 0;
						}
						yield dev.hartforge.plinthworks.logic.network.PlinthNetwork.get(level)
								.channel(be.channel()).mode().ordinal();
					}
					default -> 0;
				};
			}

			@Override
			public void set(int index, int value) {}

			@Override
			public int getCount() {
				return 7;
			}
		};
	}
}
