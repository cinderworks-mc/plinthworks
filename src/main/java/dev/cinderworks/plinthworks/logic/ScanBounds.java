package dev.cinderworks.plinthworks.logic;

public class ScanBounds
{
	public static int cubePositions(int range) {
		int side = range * 2 + 1;
		return side * side * side - 1;
	}

	public static int cappedHandlers(int found, int cap) {
		return Math.min(found, cap);
	}
}
