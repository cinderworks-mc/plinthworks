package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.component.SealConfig;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.EnumMap;

public record UpgradeSet(
		SigilType verb,
		boolean filtered,
		int intervalTicks,
		int throughput,
		int bufferSlots,
		int range,
		int capacityLevel,
		int storageLevel)
{
	public static UpgradeSet from(ItemStackHandler sigil, ItemStackHandler etchings,
			ItemStackHandler seals) {
		SigilType verb = null;
		if (sigil.getStackInSlot(0).getItem() instanceof SigilItem item) {
			verb = item.type();
		}

		EnumMap<EtchingType, Integer> levels = new EnumMap<>(EtchingType.class);
		for (int i = 0; i < etchings.getSlots(); i++) {
			ItemStack stack = etchings.getStackInSlot(i);
			if (stack.getItem() instanceof EtchingItem item) {
				levels.merge(item.type(), item.tier().level() * stack.getCount(), Integer::sum);
			}
		}

		return fromLevels(verb, hasFilterKeys(seals), levels, EtchingRates.fromConfig());
	}

	public static UpgradeSet fromLevels(SigilType verb, boolean filtered,
			EnumMap<EtchingType, Integer> levels) {
		return fromLevels(verb, filtered, levels, EtchingRates.DEFAULTS);
	}

	public static UpgradeSet fromLevels(SigilType verb, boolean filtered,
			EnumMap<EtchingType, Integer> levels, EtchingRates rates) {
		int speed = levels.getOrDefault(EtchingType.SPEED, 0);
		int capacity = levels.getOrDefault(EtchingType.CAPACITY, 0);
		int storage = levels.getOrDefault(EtchingType.STORAGE, 0);
		int range = levels.getOrDefault(EtchingType.RANGE, 0);
		return new UpgradeSet(
				verb,
				filtered,
				Math.max(rates.floorInterval(), rates.baseInterval() - speed * rates.speedStep()),
				Math.min(rates.capacityMax(), rates.baseCapacity() + capacity * rates.capacityStep()),
				Math.min(rates.storageMax(), rates.baseStorage() + storage * rates.storageStep()),
				Math.min(rates.rangeMax(), rates.baseRange() + range * rates.rangeStep()),
				capacity,
				storage);
	}

	// an unconfigured seal (no keys) is pass-through, so it doesn't count as filtering
	private static boolean hasFilterKeys(ItemStackHandler handler) {
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (stack.getItem() instanceof SealItem
					&& !stack.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY).keys().isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
