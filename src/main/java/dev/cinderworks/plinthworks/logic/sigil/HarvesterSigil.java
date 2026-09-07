package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class HarvesterSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		if (be.resourceMode() != ResourceMode.ITEM || !(be.getLevel() instanceof ServerLevel level)
				|| WorldSigilSupport.needsFilter(upgrades)) {
			return;
		}
		List<IItemHandler> handlers = WorldSigilSupport.handlers(be, upgrades);
		FakePlayer player = PlinthFakePlayer.get(level, be.getBlockPos());
		int moved = 0;
		for (BlockPos pos : WorldSigilSupport.positions(be, upgrades.range())) {
			if (moved >= upgrades.throughput() || !level.hasChunkAt(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (!mature(state)) {
				continue;
			}
			ItemStack seed = state.getBlock().getCloneItemStack(level, pos, state);
			if (!WorldSigilSupport.filterAllows(be, upgrades, seed)) {
				continue;
			}
			if (!WorldSigilSupport.breakBlock(be, level, pos.immutable(), state, handlers)) {
				continue;
			}
			moved++;
			WorldSigilSupport.Source source = WorldSigilSupport.find(handlers,
					stack -> ItemStack.isSameItem(stack, seed));
			if (source != null) {
				WorldSigilSupport.place(level, player, source, pos.below().immutable(), Direction.UP);
			}
		}
	}

	private static boolean mature(BlockState state) {
		if (state.getBlock() instanceof CropBlock crop) {
			return crop.isMaxAge(state);
		}
		if (state.getBlock() instanceof NetherWartBlock) {
			return state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE;
		}
		for (Property<?> property : state.getProperties()) {
			if (property instanceof IntegerProperty age && age.getName().equals("age")
					&& state.getValue(age).equals(age.getPossibleValues().stream().max(Integer::compareTo).orElse(0))) {
				return true;
			}
		}
		return false;
	}
}
