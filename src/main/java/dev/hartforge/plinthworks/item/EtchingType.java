package dev.hartforge.plinthworks.item;

import java.util.Locale;

public enum EtchingType
{
	SPEED,
	CAPACITY,
	STORAGE,
	RANGE;

	public String path(EtchingTier tier) {
		return "etching_" + name().toLowerCase(Locale.ROOT) + "_" + tier.level();
	}
}
