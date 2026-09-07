package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class PlacerSigil implements SigilBehavior
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
			if (moved >= upgrades.throughput() || pos.equals(be.getBlockPos()) || !level.hasChunkAt(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (!state.isAir() && !state.canBeReplaced()) {
				continue;
			}
			WorldSigilSupport.Source source = WorldSigilSupport.find(sources, stack ->
					stack.getItem() instanceof BlockItem
							&& WorldSigilSupport.filterAllows(be, upgrades, stack));
			if (source == null) {
				return;
			}
			if (WorldSigilSupport.place(level, player, source, pos.immutable(), Direction.UP)) {
				moved++;
			}
		}
	}
}
