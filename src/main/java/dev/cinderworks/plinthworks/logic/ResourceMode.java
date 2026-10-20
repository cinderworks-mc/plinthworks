package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.logic.resource.EssentiaSupport;

public enum ResourceMode
{
	ITEM,
	ENERGY,
	FLUID,
	XP,
	ESSENTIA;

	// essentia only joins the cycle when some mod provides it
	public ResourceMode next() {
		ResourceMode next = values()[(ordinal() + 1) % values().length];
		return next == ESSENTIA && !EssentiaSupport.available() ? next.next() : next;
	}

	public static ResourceMode fromOrdinal(int ordinal) {
		return values()[Math.floorMod(ordinal, values().length)];
	}
}
