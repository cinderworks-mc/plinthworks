package dev.hartforge.plinthworks.item;

import java.util.Locale;

public enum SigilType
{
	IMPORT,
	EXPORT,
	CRAFTING,
	VOID;

	public String path() {
		return "sigil_" + name().toLowerCase(Locale.ROOT);
	}
}
