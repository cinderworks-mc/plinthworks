package dev.cinderworks.plinthworks.logic;

// what the plinth model shows, kept apart from the block entity so it can be tested
public class PlinthVisual
{
	public static final int GAUGE_MAX = 4;

	// glows while a sigil is seated and redstone lets it run
	public static boolean active(boolean powered, boolean hasSigil) {
		return powered && hasSigil;
	}

	// one bar per installed etching, capped at what the side face can draw
	public static int gauge(int etchings) {
		return Math.max(0, Math.min(GAUGE_MAX, etchings));
	}
}
