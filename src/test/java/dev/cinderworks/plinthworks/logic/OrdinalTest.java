package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.net.SealEdit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrdinalTest
{
	@Test
	void redstoneModeSurvivesCorruptValues() {
		assertEquals(RedstoneMode.ALWAYS, RedstoneMode.fromOrdinal(0));
		assertEquals(RedstoneMode.RUN_WHEN_POWERED, RedstoneMode.fromOrdinal(2));
		// the old modulo threw on negatives
		assertNotNull(RedstoneMode.fromOrdinal(-1));
		assertNotNull(RedstoneMode.fromOrdinal(Integer.MIN_VALUE));
		assertNotNull(RedstoneMode.fromOrdinal(Integer.MAX_VALUE));
	}

	@Test
	void sealEditNeverIndexesOutOfBounds() {
		assertEquals(SealEdit.TOGGLE_MODE, SealEdit.fromOrdinal(0));
		assertNotNull(SealEdit.fromOrdinal(-7));
		assertNotNull(SealEdit.fromOrdinal(Integer.MIN_VALUE));
		assertNotNull(SealEdit.fromOrdinal(Integer.MAX_VALUE));
	}
}
