package dev.cinderworks.plinthworks.item;

import java.util.Locale;

public enum SigilType
{
	IMPORT,
	EXPORT,
	CRAFTING,
	VOID,
	BREAKER,
	PLACER,
	PLANTER,
	HARVESTER,
	MAGNET;

	public String path() {
		return "sigil_" + name().toLowerCase(Locale.ROOT);
	}
}
