package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class PlanterSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		if (be.resourceMode() != ResourceMode.ITEM || !(be.getLevel() instanceof ServerLevel level)) {
			return;
		}
		List<IItemHandler> sources = WorldSigilSupport.sources(be, upgrades);
		FakePlayer player = PlinthFakePlayer.get(level, be.getBlockPos());
		int moved = 0;
		for (BlockPos pos : WorldSigilSupport.positions(be, upgrades.range())) {
			BlockPos soil = pos.below();
			if (moved >= upgrades.throughput() || !level.hasChunkAt(soil) || !level.hasChunkAt(pos)
					|| !level.getBlockState(pos).isAir() || !isSoil(level.getBlockState(soil))) {
				continue;
			}
			WorldSigilSupport.Source source = WorldSigilSupport.find(sources, stack ->
					WorldSigilSupport.filterAllows(be, upgrades, stack) && isPlant(stack, level, pos));
			if (source == null) {
				return;
			}
			if (WorldSigilSupport.place(level, player, source, soil.immutable(), Direction.UP)) {
				moved++;
			}
		}
	}

	private static boolean isSoil(net.minecraft.world.level.block.state.BlockState state) {
		return state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK);
	}

	private static boolean isPlant(ItemStack stack, ServerLevel level, BlockPos pos) {
		if (stack.getItem() instanceof SpecialPlantable plantable
				&& plantable.canPlacePlantAtPosition(stack, level, pos, Direction.DOWN)) {
			return true;
		}
		if (!(stack.getItem() instanceof BlockItem item)) {
			return false;
		}
		Block block = item.getBlock();
		return (block instanceof CropBlock || block instanceof SaplingBlock || block instanceof StemBlock)
				&& block.defaultBlockState().canSurvive(level, pos);
	}
}
