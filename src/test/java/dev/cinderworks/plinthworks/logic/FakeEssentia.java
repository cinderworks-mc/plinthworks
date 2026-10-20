package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.logic.resource.Essentia;
import dev.cinderworks.plinthworks.logic.resource.EssentiaHandler;
import dev.cinderworks.plinthworks.logic.resource.EssentiaProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// a provider with no world behind it: finds nothing and counts what it was asked to release
class FakeEssentia implements EssentiaProvider
{
	static final ResourceLocation AER = ResourceLocation.fromNamespaceAndPath("fake", "aer");
	static final ResourceLocation IGNIS = ResourceLocation.fromNamespaceAndPath("fake", "ignis");

	final Map<ResourceLocation, Integer> released = new HashMap<>();

	@Override
	public EssentiaHandler find(Level level, BlockPos pos, Direction side) {
		return null;
	}

	@Override
	public void release(Level level, BlockPos pos, ResourceLocation aspect, int points) {
		released.merge(aspect, points, Integer::sum);
	}

	int released() {
		return released.values().stream().mapToInt(Integer::intValue).sum();
	}

	// a jar: one aspect, a label that pins it, and a switch to make it refuse refunds
	static class Jar implements EssentiaHandler
	{
		ResourceLocation aspect;
		ResourceLocation label;
		int amount;
		int capacity;
		boolean refuseInserts;
		// says yes when simulating, then takes only this many for real
		int realInsertCap = Integer.MAX_VALUE;

		Jar(ResourceLocation aspect, int amount, int capacity) {
			this.aspect = amount > 0 ? aspect : null;
			this.amount = amount;
			this.capacity = capacity;
		}

		@Override
		public List<Essentia> contents() {
			return amount > 0 ? List.of(new Essentia(aspect, amount)) : List.of();
		}

		@Override
		public int insert(ResourceLocation id, int points, boolean simulate) {
			if (refuseInserts || (label != null && !label.equals(id)) || (amount > 0 && !id.equals(aspect))) {
				return 0;
			}
			int moved = Math.min(points, capacity - amount);
			if (!simulate) {
				moved = Math.min(moved, realInsertCap);
			}
			if (!simulate && moved > 0) {
				aspect = id;
				amount += moved;
			}
			return Math.max(0, moved);
		}

		@Override
		public int extract(ResourceLocation id, int points, boolean simulate) {
			if (amount <= 0 || !id.equals(aspect)) {
				return 0;
			}
			int moved = Math.min(points, amount);
			if (!simulate) {
				amount -= moved;
				if (amount == 0) {
					aspect = null;
				}
			}
			return moved;
		}
	}
}
