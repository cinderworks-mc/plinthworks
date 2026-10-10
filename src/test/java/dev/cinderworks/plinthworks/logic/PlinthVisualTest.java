package dev.cinderworks.plinthworks.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlinthVisualTest
{
	@Test
	void glowNeedsASigilAndPower() {
		assertTrue(PlinthVisual.active(true, true));
		assertFalse(PlinthVisual.active(false, true));
		assertFalse(PlinthVisual.active(true, false));
		assertFalse(PlinthVisual.active(false, false));
	}

	@Test
	void gaugeStaysInsideTheDrawnBars() {
		assertEquals(0, PlinthVisual.gauge(0));
		assertEquals(3, PlinthVisual.gauge(3));
		assertEquals(PlinthVisual.GAUGE_MAX, PlinthVisual.gauge(8));
		assertEquals(0, PlinthVisual.gauge(-1));
	}

	@Test
	void everyResourceModeHasAGlyphSlot() {
		// the blockstate mode property is the ordinal, 0 to 3
		assertEquals(4, ResourceMode.values().length);
	}
}
