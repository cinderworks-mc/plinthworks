package dev.hartforge.plinthworks.item;

import java.util.Locale;

public enum SealType
{
	ITEM,
	ITEM_EXACT,
	MOD,
	TAG,
	COMPONENT;

	public String path() {
		return "seal_" + name().toLowerCase(Locale.ROOT);
	}
}
