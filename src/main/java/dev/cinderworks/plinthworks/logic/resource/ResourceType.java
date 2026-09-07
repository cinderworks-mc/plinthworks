package dev.cinderworks.plinthworks.logic.resource;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.logic.UpgradeSet;
import dev.cinderworks.plinthworks.registry.ModBlocks;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Predicate;

public interface ResourceType<T, R>
{
	T find(Level level, BlockPos pos, Direction side);

	T buffer(PlinthBlockEntity be);

	Predicate<R> filter(PlinthBlockEntity be, UpgradeSet upgrades);

	int move(T source, T dest, int amount, Predicate<R> filter);

	int discard(T source, int amount, Predicate<R> filter);

	int throughput(UpgradeSet upgrades);

	default List<T> nearby(PlinthBlockEntity be, int range) {
		Level level = be.getLevel();
		BlockPos center = be.getBlockPos();
		int cap = ModConfig.MAX_HANDLERS.get();
		List<T> handlers = new ArrayList<>();
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
		return handlers;
	}

	default List<T> adjacent(Level level, BlockPos center) {
		List<T> handlers = new ArrayList<>();
		for (Direction dir : Direction.values()) {
			add(level, center.relative(dir), dir.getOpposite(), handlers, ModConfig.MAX_HANDLERS.get());
		}
		return handlers;
	}

	private void add(Level level, BlockPos pos, Direction side, List<T> handlers, int cap) {
		if (handlers.size() >= cap || level.getBlockState(pos).is(ModBlocks.PLINTH.get())) {
			return;
		}
		T handler = find(level, pos, side);
		if (handler != null) {
			handlers.add(handler);
		}
	}
}
