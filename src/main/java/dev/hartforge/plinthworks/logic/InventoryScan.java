package dev.hartforge.plinthworks.logic;

import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.config.ModConfig;
import dev.hartforge.plinthworks.registry.ModBlocks;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class InventoryScan
{
	public static List<IItemHandler> nearbyHandlers(PlinthBlockEntity be, int range) {
		Level level = be.getLevel();
		if (be.hasFreshHandlerCache(level.getGameTime())) {
			return be.getCachedHandlers();
		}

		List<IItemHandler> handlers = new ArrayList<>();
		BlockPos center = be.getBlockPos();
		int cap = ModConfig.MAX_HANDLERS.get();
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

		be.setHandlerCache(handlers, level.getGameTime() + ModConfig.RESCAN_INTERVAL.get());
		return handlers;
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
