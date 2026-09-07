package dev.cinderworks.plinthworks.logic;

public enum ResourceMode
{
	ITEM,
	ENERGY,
	FLUID,
	XP;

	public ResourceMode next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public static ResourceMode fromOrdinal(int ordinal) {
		return values()[Math.floorMod(ordinal, values().length)];
	}
}
