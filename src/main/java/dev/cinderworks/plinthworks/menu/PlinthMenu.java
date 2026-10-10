package dev.cinderworks.plinthworks.menu;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.component.*;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import dev.cinderworks.plinthworks.logic.UpgradeSet;
import dev.cinderworks.plinthworks.logic.resource.ResourceTypes;
import dev.cinderworks.plinthworks.logic.seal.SealMatch;
import dev.cinderworks.plinthworks.net.SealEdit;
import dev.cinderworks.plinthworks.registry.*;
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
	private static final int KEY_INDEX_LIMIT = 64;

	private final PlinthBlockEntity be;
	private final ContainerData stats;
	private final int plinthSlots;
	private final java.util.List<BlockPos> networkMembers;

	public PlinthMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf data) {
		this(id, playerInv, data.readBlockPos(), readMembers(data));
	}

	public PlinthMenu(int id, Inventory playerInv, BlockPos pos) {
		this(id, playerInv, pos, java.util.List.of());
	}

	private PlinthMenu(int id, Inventory playerInv, BlockPos pos, java.util.List<BlockPos> members) {
		this(id, playerInv, (PlinthBlockEntity) playerInv.player.level().getBlockEntity(pos),
				new SimpleContainerData(22), members);
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
		addSlot(new SigilSlot(be, be.sigil(), 0, 10, 29));
		for (int i = 0; i < be.etchings().getSlots(); i++) {
			addSlot(new EtchingSlot(be, be.etchings(), i, 50 + i * 18, 29));
		}
		for (int i = 0; i < be.seals().getSlots(); i++) {
			addSlot(new SealSlot(be, be.seals(), i, 62 + i * 20, 67));
		}
		addSlot(new SlotItemHandler(be.display(), 0, 10, 63) {
			@Override
			public boolean isActive() {
				return PlinthMenu.this.resourceMode() == ResourceMode.ITEM;
			}

			@Override
			public void setChanged() {
				PlinthMenu.this.be.slotsChanged();
			}
		});
		addPlayerSlots(playerInv);
		addDataSlots(stats);
	}

	private void addPlayerSlots(Inventory inventory) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 9 + col * 18, 134 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 9 + col * 18, 192));
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
		if (id == 20 && be.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& !be.channel().isEmpty()) {
			dev.cinderworks.plinthworks.logic.network.PlinthNetwork.get(serverLevel).cycleMode(be.channel());
			broadcastChanges();
			return true;
		}
		if (id == 60) {
			be.cycleRedstoneMode();
			broadcastChanges();
			return true;
		}
		if (id == 80) {
			be.cycleResourceMode();
			broadcastChanges();
			return true;
		}
		// ghost-slot seal editor (ITEM / ITEM_EXACT / MOD) opens as its own menu
		if (id >= 70 && id < 70 + be.seals().getSlots()) {
			int slot = id - 70;
			if (player instanceof net.minecraft.server.level.ServerPlayer sp
					&& be.seals().getStackInSlot(slot).getItem() instanceof SealItem) {
				dev.cinderworks.plinthworks.block.PlinthBlock.openSealMenu(sp, be, be.getBlockPos(), slot);
			}
			return true;
		}
		return false;
	}

	// the payload already sanitized the name
	public void applyChannel(String name) {
		if (!(be.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
			return;
		}
		dev.cinderworks.plinthworks.logic.network.PlinthNetwork.get(serverLevel).add(name, be.getBlockPos());
		be.setChannel(name);
		broadcastChanges();
	}

	public void applySealEdit(SealEdit action, int index, int key) {
		if (index < 0 || index >= be.seals().getSlots() || key < 0 || key >= KEY_INDEX_LIMIT) {
			return;
		}
		ItemStack sealStack = be.seals().getStackInSlot(index);
		if (!(sealStack.getItem() instanceof SealItem seal)) {
			return;
		}
		sealStack = sealStack.copy();
		SealConfig config = sealStack.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY);
		switch (action) {
			case TOGGLE_MODE -> sealStack.set(ModDataComponents.SEAL_CONFIG.get(), config.toggleMode());
			case CLEAR_KEYS -> sealStack.set(ModDataComponents.SEAL_CONFIG.get(),
					new SealConfig(config.whitelist(), java.util.List.of()));
			default -> {
				MatchKey match = keyFor(seal.type(), action, key);
				if (match == null) {
					return;
				}
				sealStack.set(ModDataComponents.SEAL_CONFIG.get(), config.with(match));
			}
		}
		be.seals().setStackInSlot(index, sealStack);
		broadcastChanges();
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

	public ResourceMode resourceMode() {
		return ResourceMode.fromOrdinal(stats.get(7));
	}

	public int energyStored() {
		return wide(8);
	}

	public int energyCapacity() {
		return wide(10);
	}

	public int energyThroughput() {
		return wide(12);
	}

	public int fluidStored() {
		return wide(14);
	}

	public int fluidCapacity() {
		return wide(16);
	}

	public int fluidThroughput() {
		return wide(18);
	}

	public int xpMbPerPoint() {
		return wide(20);
	}

	private int wide(int index) {
		return (stats.get(index) & 0xffff) | (stats.get(index + 1) & 0xffff) << 16;
	}

	public java.util.List<BlockPos> networkMembers() {
		return networkMembers;
	}

	public ItemStack seal(int index) {
		return be.seals().getStackInSlot(index);
	}

	// the action has to match the seal type
	private MatchKey keyFor(SealType type, SealEdit action, int key) {
		ItemStack shown = be.getDisplayedItem();
		if (shown.isEmpty()) {
			return null;
		}
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(shown.getItem());
		return switch (action) {
			case ADD_DISPLAYED -> switch (type) {
				case ITEM -> new MatchKey.ItemKey(itemId);
				case ITEM_EXACT -> new MatchKey.ComponentKey(Plinthworks.id("exact"),
						SealMatch.componentFingerprint(shown));
				case MOD -> new MatchKey.ModKey(itemId.getNamespace());
				default -> null;
			};
			case ADD_TAG -> type != SealType.TAG ? null : shown.getTags().skip(key).findFirst()
					.map(tag -> (MatchKey) new MatchKey.TagKey(tag.location())).orElse(null);
			case ADD_COMPONENT -> type != SealType.COMPONENT ? null
					: shown.getComponents().stream().skip(key).findFirst()
							.map(component -> componentKey(component)).orElse(null);
			default -> null;
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
						yield dev.cinderworks.plinthworks.logic.network.PlinthNetwork.get(level)
								.channel(be.channel()).mode().ordinal();
					}
					case 7 -> be.resourceMode().ordinal();
					case 8 -> be.energy().getEnergyStored() & 0xffff;
					case 9 -> be.energy().getEnergyStored() >>> 16;
					case 10 -> be.energy().getMaxEnergyStored() & 0xffff;
					case 11 -> be.energy().getMaxEnergyStored() >>> 16;
					case 12 -> ResourceTypes.ENERGY.throughput(u) & 0xffff;
					case 13 -> ResourceTypes.ENERGY.throughput(u) >>> 16;
					case 14 -> be.fluid().getFluidAmount() & 0xffff;
					case 15 -> be.fluid().getFluidAmount() >>> 16;
					case 16 -> be.fluid().getCapacity() & 0xffff;
					case 17 -> be.fluid().getCapacity() >>> 16;
					case 18 -> ResourceTypes.FLUID.throughput(u) & 0xffff;
					case 19 -> ResourceTypes.FLUID.throughput(u) >>> 16;
					case 20 -> dev.cinderworks.plinthworks.config.ModConfig.XP_MB_PER_POINT.get() & 0xffff;
					case 21 -> dev.cinderworks.plinthworks.config.ModConfig.XP_MB_PER_POINT.get() >>> 16;
					default -> 0;
				};
			}

			@Override
			public void set(int index, int value) {}

			@Override
			public int getCount() {
				return 22;
			}
		};
	}
}
