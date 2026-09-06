package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.registry.ModBlocks;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class InventoryScan
{
	// re-queried every op rather than cached - a stale handler ref can void inserts
	// into a container that was broken or replaced mid-window
	public static List<IItemHandler> nearbyHandlers(PlinthBlockEntity be, int range) {
		Level level = be.getLevel();
		BlockPos center = be.getBlockPos();
		int cap = ModConfig.MAX_HANDLERS.get();
		List<IItemHandler> handlers = new ArrayList<>(Math.min(cap, range == 1 ? 6 : ScanBounds.cubePositions(range)));
		if (range == 1) {
			for (Direction dir : Direction.values()) {
				add(level, center.relative(dir), dir.getOpposite(), handlers, cap);
			}
		} else {
			outer:
			for (int x = -range; x <= range; x++) {
				for (int y = -range; y <= range; y++) {
					for (int z = -range; z <= range; z++) {
						if (x == 0 && y == 0 && z == 0) {
							continue;
						}
						add(level, center.offset(x, y, z), null, handlers, cap);
						if (handlers.size() >= cap) {
							break outer;
						}
					}
				}
			}
		}

		int keep = ScanBounds.cappedHandlers(handlers.size(), cap);
		return keep == handlers.size() ? handlers : handlers.subList(0, keep);
	}

	public static List<IItemHandler> adjacentHandlers(Level level, BlockPos center) {
		List<IItemHandler> handlers = new ArrayList<>();
		for (Direction dir : Direction.values()) {
			add(level, center.relative(dir), dir.getOpposite(), handlers, ModConfig.MAX_HANDLERS.get());
		}
		return handlers;
	}

	private static void add(Level level, BlockPos pos, Direction side, List<IItemHandler> handlers, int cap) {
		if (handlers.size() >= cap || level.getBlockState(pos).is(ModBlocks.PLINTH.get())) {
			return;
		}
		IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
		if (handler != null) {
			handlers.add(handler);
		}
	}
}
