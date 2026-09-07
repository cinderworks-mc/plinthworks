package dev.cinderworks.plinthworks.config;

import dev.cinderworks.plinthworks.logic.EtchingRates;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ModConfig
{
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	private static final EtchingRates D = EtchingRates.DEFAULTS;

	public static final ModConfigSpec.IntValue BASE_INTERVAL;
	public static final ModConfigSpec.IntValue SPEED_STEP;
	public static final ModConfigSpec.IntValue SPEED_FLOOR_INTERVAL;
	public static final ModConfigSpec.IntValue BASE_CAPACITY;
	public static final ModConfigSpec.IntValue CAPACITY_STEP;
	public static final ModConfigSpec.IntValue CAPACITY_MAX;
	public static final ModConfigSpec.IntValue BASE_STORAGE;
	public static final ModConfigSpec.IntValue STORAGE_STEP;
	public static final ModConfigSpec.IntValue STORAGE_MAX;
	public static final ModConfigSpec.IntValue BASE_RANGE;
	public static final ModConfigSpec.IntValue RANGE_STEP;
	public static final ModConfigSpec.IntValue RANGE_MAX;
	public static final ModConfigSpec.IntValue ETCHING_SLOTS;
	public static final ModConfigSpec.IntValue SEAL_SLOTS;
	public static final ModConfigSpec.IntValue MAX_AREA_VOLUME;
	public static final ModConfigSpec.BooleanValue FUEL_COSTS;
	public static final ModConfigSpec.IntValue MAX_HANDLERS;
	public static final ModConfigSpec.IntValue LINK_RANGE;
	public static final ModConfigSpec.BooleanValue CRAFTING_ENABLED;
	public static final ModConfigSpec.BooleanValue BREAK_BLOCK_ENTITIES;
	public static final ModConfigSpec.BooleanValue REQUIRE_FILTER;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_BLOCKLIST;
	public static final ModConfigSpec.BooleanValue FIRE_BLOCK_EVENTS;
	public static final ModConfigSpec.IntValue ENERGY_BASE_STORAGE;
	public static final ModConfigSpec.IntValue ENERGY_STORAGE_STEP;
	public static final ModConfigSpec.IntValue ENERGY_STORAGE_MAX;
	public static final ModConfigSpec.IntValue ENERGY_BASE_TRANSFER;
	public static final ModConfigSpec.IntValue ENERGY_TRANSFER_STEP;
	public static final ModConfigSpec.IntValue ENERGY_TRANSFER_MAX;
	public static final ModConfigSpec.IntValue FLUID_BASE_STORAGE;
	public static final ModConfigSpec.IntValue FLUID_STORAGE_STEP;
	public static final ModConfigSpec.IntValue FLUID_STORAGE_MAX;
	public static final ModConfigSpec.IntValue FLUID_BASE_TRANSFER;
	public static final ModConfigSpec.IntValue FLUID_TRANSFER_STEP;
	public static final ModConfigSpec.IntValue FLUID_TRANSFER_MAX;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> XP_FLUIDS;
	public static final ModConfigSpec.IntValue XP_MB_PER_POINT;
	public static final ModConfigSpec SPEC;

	static {
		BUILDER.push("speed");
		BASE_INTERVAL = BUILDER.defineInRange("baseInterval", D.baseInterval(), 1, 1200);
		SPEED_STEP = BUILDER.defineInRange("step", D.speedStep(), 1, 100);
		SPEED_FLOOR_INTERVAL = BUILDER.defineInRange("floorInterval", D.floorInterval(), 1, 1200);
		BUILDER.pop();

		BUILDER.push("capacity");
		BASE_CAPACITY = BUILDER.defineInRange("base", D.baseCapacity(), 1, 64);
		CAPACITY_STEP = BUILDER.defineInRange("step", D.capacityStep(), 1, 64);
		CAPACITY_MAX = BUILDER.defineInRange("max", D.capacityMax(), 1, 64);
		BUILDER.pop();

		BUILDER.push("storage");
		BASE_STORAGE = BUILDER.defineInRange("base", D.baseStorage(), 1, 64);
		STORAGE_STEP = BUILDER.defineInRange("step", D.storageStep(), 1, 64);
		STORAGE_MAX = BUILDER.defineInRange("max", D.storageMax(), 1, 64);
		BUILDER.pop();

		BUILDER.push("range");
		BASE_RANGE = BUILDER.defineInRange("base", D.baseRange(), 1, 16);
		RANGE_STEP = BUILDER.defineInRange("step", D.rangeStep(), 1, 16);
		RANGE_MAX = BUILDER.defineInRange("max", D.rangeMax(), 1, 128);
		MAX_HANDLERS = BUILDER.defineInRange("maxHandlers", 64, 1, 512);
		BUILDER.pop();

		BUILDER.push("link");
		LINK_RANGE = BUILDER.defineInRange("range", 24, 1, 256);
		BUILDER.pop();

		BUILDER.push("etchings");
		ETCHING_SLOTS = BUILDER.defineInRange("slots", 4, 1, 8);
		BUILDER.pop();

		BUILDER.push("seals");
		SEAL_SLOTS = BUILDER.defineInRange("slots", 3, 1, 8);
		BUILDER.pop();

		BUILDER.push("crafting");
		CRAFTING_ENABLED = BUILDER.define("enabled", true);
		BUILDER.pop();

		BUILDER.push("world");
		BUILDER.push("verbs");
		BREAK_BLOCK_ENTITIES = BUILDER.define("breakBlockEntities", false);
		REQUIRE_FILTER = BUILDER.define("requireFilter", false);
		BLOCK_BLOCKLIST = BUILDER.defineList("blockBlocklist", List.of(), () -> "modid:block",
				value -> value instanceof String entry && !entry.isBlank());
		FIRE_BLOCK_EVENTS = BUILDER.define("fireBlockEvents", true);
		BUILDER.pop();
		BUILDER.pop();

		BUILDER.push("energy");
		ENERGY_BASE_STORAGE = BUILDER.defineInRange("baseStorage", 40000, 1, Integer.MAX_VALUE);
		ENERGY_STORAGE_STEP = BUILDER.defineInRange("storageStep", 40000, 1, Integer.MAX_VALUE);
		ENERGY_STORAGE_MAX = BUILDER.defineInRange("storageMax", 320000, 1, Integer.MAX_VALUE);
		ENERGY_BASE_TRANSFER = BUILDER.defineInRange("baseTransfer", 1000, 1, Integer.MAX_VALUE);
		ENERGY_TRANSFER_STEP = BUILDER.defineInRange("transferStep", 4000, 1, Integer.MAX_VALUE);
		ENERGY_TRANSFER_MAX = BUILDER.defineInRange("transferMax", 32000, 1, Integer.MAX_VALUE);
		BUILDER.pop();

		BUILDER.push("fluid");
		FLUID_BASE_STORAGE = BUILDER.defineInRange("baseStorage", 8000, 1, Integer.MAX_VALUE);
		FLUID_STORAGE_STEP = BUILDER.defineInRange("storageStep", 8000, 1, Integer.MAX_VALUE);
		FLUID_STORAGE_MAX = BUILDER.defineInRange("storageMax", 64000, 1, Integer.MAX_VALUE);
		FLUID_BASE_TRANSFER = BUILDER.defineInRange("baseTransfer", 250, 1, Integer.MAX_VALUE);
		FLUID_TRANSFER_STEP = BUILDER.defineInRange("transferStep", 1000, 1, Integer.MAX_VALUE);
		FLUID_TRANSFER_MAX = BUILDER.defineInRange("transferMax", 8000, 1, Integer.MAX_VALUE);
		BUILDER.pop();

		BUILDER.push("xp");
		XP_FLUIDS = BUILDER.defineList("fluids", List.of("create_enchantment_industry:experience",
				"#c:experience", "#forge:experience"), () -> "modid:fluid",
				value -> value instanceof String entry && !entry.isBlank());
		XP_MB_PER_POINT = BUILDER.defineInRange("mbPerPoint", 20, 1, Integer.MAX_VALUE);
		BUILDER.pop();

		BUILDER.push("area");
		MAX_AREA_VOLUME = BUILDER.defineInRange("maxVolume", 4096, 1, 32768);
		BUILDER.pop();

		BUILDER.push("fuelProfile");
		FUEL_COSTS = BUILDER.define("enabled", false);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}
}
