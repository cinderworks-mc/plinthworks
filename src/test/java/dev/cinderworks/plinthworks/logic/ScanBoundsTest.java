package dev.cinderworks.plinthworks.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScanBoundsTest
{
	@Test
	void countsCubeWithoutCenter() {
		assertEquals(26, ScanBounds.cubePositions(1));
		assertEquals(728, ScanBounds.cubePositions(4));
		assertEquals(64, ScanBounds.cappedHandlers(80, 64));
	}
}
