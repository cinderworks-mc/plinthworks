package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.config.ModConfig;

// the tunable etching ladder, pulled out of the config so the summation math can be
// tested without a loaded config. DEFAULTS also seeds ModConfig so the two never drift.
public record EtchingRates(
		int baseInterval, int speedStep, int floorInterval,
		int baseCapacity, int capacityStep, int capacityMax,
		int baseStorage, int storageStep, int storageMax,
		int baseRange, int rangeStep, int rangeMax)
{
	public static final EtchingRates DEFAULTS = new EtchingRates(
			20, 3, 2,
			1, 8, 64,
			1, 1, 8,
			1, 1, 16);

	public static EtchingRates fromConfig() {
		return new EtchingRates(
				ModConfig.BASE_INTERVAL.get(), ModConfig.SPEED_STEP.get(), ModConfig.SPEED_FLOOR_INTERVAL.get(),
				ModConfig.BASE_CAPACITY.get(), ModConfig.CAPACITY_STEP.get(), ModConfig.CAPACITY_MAX.get(),
				ModConfig.BASE_STORAGE.get(), ModConfig.STORAGE_STEP.get(), ModConfig.STORAGE_MAX.get(),
				ModConfig.BASE_RANGE.get(), ModConfig.RANGE_STEP.get(), ModConfig.RANGE_MAX.get());
	}
}
