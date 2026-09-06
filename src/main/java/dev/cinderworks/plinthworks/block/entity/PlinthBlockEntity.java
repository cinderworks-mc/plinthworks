package dev.cinderworks.plinthworks.block.entity;

import dev.cinderworks.plinthworks.component.*;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.*;
import dev.cinderworks.plinthworks.logic.seal.SealMatch;
import dev.cinderworks.plinthworks.logic.seal.SealMatch.SealEntry;
import dev.cinderworks.plinthworks.registry.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.*;

import java.util.List;

public class PlinthBlockEntity extends BlockEntity
{
	private final ItemStackHandler display = new ItemStackHandler(1) {
		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return stack.getMaxStackSize() * upgrades().bufferSlots();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return !upgrades().filtered() || matchesFilter(stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			changedAndSync();
		}
	};
	private final ItemStackHandler sigil = new ItemStackHandler(1) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.getItem() instanceof SigilItem;
		}

		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}

		@Override
		protected void onContentsChanged(int slot) {
			upgradesChanged();
		}
	};
	private final ItemStackHandler etchings = new ItemStackHandler(ModConfig.ETCHING_SLOTS.get()) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.getItem() instanceof EtchingItem;
		}

		@Override
		protected void onContentsChanged(int slot) {
			upgradesChanged();
		}
	};
	private final ItemStackHandler seals = new ItemStackHandler(ModConfig.SEAL_SLOTS.get()) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.getItem() instanceof SealItem;
		}

		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}

		@Override
		protected void onContentsChanged(int slot) {
			upgradesChanged();
		}
	};
	private BlockState baseState = Blocks.STONE.defaultBlockState();
	private UpgradeSet cachedUpgrades;
	private int cooldown;
	private String channel = "";
	private RedstoneMode redstoneMode = RedstoneMode.ALWAYS;

	public PlinthBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PLINTH.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, PlinthBlockEntity be) {
		if (!be.redstoneAllows()) {
			return;
		}
		UpgradeSet upgrades = be.upgrades();
		if (upgrades.verb() == null) {
			return;
		}
		if (be.cooldown-- > 0) {
			return;
		}
		be.cooldown = upgrades.intervalTicks();
		PlinthTick.run(be, upgrades);
	}

	public boolean putDisplayedItem(Player player, ItemStack held) {
		if (!display.getStackInSlot(0).isEmpty()) {
			return false;
		}
		ItemStack one = held.copyWithCount(1);
		if (!display.insertItem(0, one, false).isEmpty()) {
			return false;
		}
		held.shrink(1);
		return true;
	}

	public boolean takeDisplayedItem(Player player) {
		ItemStack stack = display.extractItem(0, display.getStackInSlot(0).getCount(), false);
		if (stack.isEmpty()) {
			return false;
		}
		ItemHandlerHelper.giveItemToPlayer(player, stack);
		return true;
	}

	public void dropStoredItems() {
		if (level == null || level.isClientSide) {
			return;
		}
		dropHandler(display);
		dropHandler(sigil);
		dropHandler(etchings);
		dropHandler(seals);
	}

	private void dropHandler(ItemStackHandler handler) {
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
						worldPosition.getZ() + 0.5, stack.copy());
				handler.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
	}

	public UpgradeSet upgrades() {
		if (cachedUpgrades == null) {
			cachedUpgrades = UpgradeSet.from(sigil, etchings, seals);
		}
		return cachedUpgrades;
	}

	public boolean matchesFilter(ItemStack stack) {
		java.util.ArrayList<SealEntry> bank = new java.util.ArrayList<>();
		for (int i = 0; i < seals.getSlots(); i++) {
			ItemStack installed = seals.getStackInSlot(i);
			if (installed.getItem() instanceof SealItem seal) {
				bank.add(new SealEntry(seal.type(),
						installed.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY)));
			}
		}
		return SealMatch.bankAllows(bank, stack);
	}

	public ItemStackHandler display() {
		return display;
	}

	public ItemStackHandler sigil() {
		return sigil;
	}

	public ItemStackHandler etchings() {
		return etchings;
	}

	public ItemStackHandler seals() {
		return seals;
	}

	public ItemStack getDisplayedItem() {
		return display.getStackInSlot(0);
	}

	public IItemHandler getAutomationHandler() {
		return display;
	}

	public BlockState getBaseState() {
		return baseState;
	}

	public void setBaseState(BlockState state) {
		baseState = state;
		changedAndSync();
	}

	public String channel() {
		return channel;
	}

	public void setChannel(String channel) {
		if (this.channel.equals(channel)) {
			return;
		}
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			dev.cinderworks.plinthworks.logic.network.PlinthNetwork net =
					dev.cinderworks.plinthworks.logic.network.PlinthNetwork.get(serverLevel);
			if (!this.channel.isEmpty()) {
				net.remove(this.channel, worldPosition);
			}
			if (!channel.isEmpty()) {
				net.add(channel, worldPosition);
			}
		}
		this.channel = channel;
		changedAndSync();
	}

	public RedstoneMode redstoneMode() {
		return redstoneMode;
	}

	public void cycleRedstoneMode() {
		redstoneMode = redstoneMode.next();
		changedAndSync();
	}

	public boolean redstoneAllows() {
		return level == null || redstoneMode.allows(level.hasNeighborSignal(worldPosition));
	}

	public int channelMembers() {
		if (channel.isEmpty() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
			return 0;
		}
		return dev.cinderworks.plinthworks.logic.network.PlinthNetwork.get(serverLevel)
				.channel(channel).members().size();
	}

	public List<BlockPos> networkMembers() {
		if (channel.isEmpty() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
			return List.of();
		}
		return dev.cinderworks.plinthworks.logic.network.PlinthNetwork.get(serverLevel)
				.channel(channel).members();
	}

	private void upgradesChanged() {
		cachedUpgrades = null;
		changedAndSync();
	}

	private void changedAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);
		display.deserializeNBT(registries, compound.getCompound("Display"));
		sigil.deserializeNBT(registries, compound.getCompound("Sigil"));
		etchings.deserializeNBT(registries, compound.getCompound("Etchings"));
		seals.deserializeNBT(registries, compound.getCompound("Seals"));
		baseState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), compound.getCompound("BaseState"));
		cooldown = compound.getInt("Cooldown");
		channel = compound.getString("Channel");
		redstoneMode = RedstoneMode.values()[compound.getInt("RedstoneMode") % RedstoneMode.values().length];
		cachedUpgrades = null;
	}

	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);
		compound.put("Display", display.serializeNBT(registries));
		compound.put("Sigil", sigil.serializeNBT(registries));
		compound.put("Etchings", etchings.serializeNBT(registries));
		compound.put("Seals", seals.serializeNBT(registries));
		compound.put("BaseState", NbtUtils.writeBlockState(baseState));
		compound.putInt("Cooldown", cooldown);
		compound.putString("Channel", channel);
		compound.putInt("RedstoneMode", redstoneMode.ordinal());
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		saveAdditional(tag, registries);
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
		handleUpdateTag(packet.getTag(), registries);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		loadAdditional(tag, registries);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(ModDataComponents.PLINTH_CONTENTS.get(),
				new PlinthContents(BuiltInRegistries.BLOCK.getKey(baseState.getBlock())));
		components.set(ModDataComponents.PLINTH_LOADOUT.get(), new PlinthLoadout(
				sigil.getStackInSlot(0), stacks(etchings), stacks(seals)));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		PlinthContents contents = input.get(ModDataComponents.PLINTH_CONTENTS.get());
		if (contents != null) {
			baseState = BuiltInRegistries.BLOCK.get(contents.baseBlock()).defaultBlockState();
		}
		PlinthLoadout loadout = input.get(ModDataComponents.PLINTH_LOADOUT.get());
		if (loadout != null) {
			sigil.setStackInSlot(0, loadout.sigil());
			putStacks(etchings, loadout.etchings());
			putStacks(seals, loadout.seals());
		}
	}

	private static List<ItemStack> stacks(ItemStackHandler handler) {
		java.util.ArrayList<ItemStack> stacks = new java.util.ArrayList<>();
		for (int i = 0; i < handler.getSlots(); i++) {
			stacks.add(handler.getStackInSlot(i));
		}
		return stacks;
	}

	private static void putStacks(ItemStackHandler handler, List<ItemStack> stacks) {
		for (int i = 0; i < handler.getSlots() && i < stacks.size(); i++) {
			handler.setStackInSlot(i, stacks.get(i));
		}
	}
}
