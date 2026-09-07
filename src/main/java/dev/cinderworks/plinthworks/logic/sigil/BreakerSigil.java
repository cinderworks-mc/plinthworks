package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.logic.*;
import dev.cinderworks.plinthworks.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class BreakerSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		if (be.resourceMode() != ResourceMode.ITEM || !(be.getLevel() instanceof ServerLevel level)
				|| WorldSigilSupport.needsFilter(upgrades)) {
			return;
		}
		List<IItemHandler> dests = WorldSigilSupport.handlers(be, upgrades);
		int moved = 0;
		for (BlockPos pos : WorldSigilSupport.positions(be, upgrades.range())) {
			if (moved >= upgrades.throughput() || pos.equals(be.getBlockPos()) || !level.hasChunkAt(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (state.isAir() || state.is(ModBlocks.PLINTH.get()) || state.getDestroySpeed(level, pos) < 0
					|| state.hasBlockEntity() && !ModConfig.BREAK_BLOCK_ENTITIES.get()
					|| WorldSigilSupport.blocked(state)) {
				continue;
			}
			ItemStack block = new ItemStack(state.getBlock().asItem());
			if (!WorldSigilSupport.filterAllows(be, upgrades, block)) {
				continue;
			}
			if (WorldSigilSupport.breakBlock(be, level, pos.immutable(), state, dests)) {
				moved++;
			}
		}
	}
}
