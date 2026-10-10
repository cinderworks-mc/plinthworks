package dev.cinderworks.plinthworks.net;

public enum SealEdit
{
	TOGGLE_MODE,
	CLEAR_KEYS,
	ADD_DISPLAYED,
	ADD_TAG,
	ADD_COMPONENT;

	// the wire can send anything
	public static SealEdit fromOrdinal(int ordinal) {
		return values()[Math.floorMod(ordinal, values().length)];
	}
}
