package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.logic.resource.EssentiaSupport;
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
	void essentiaIsOnlyOfferedWithAProvider() {
		assertFalse(EssentiaSupport.available());
		assertEquals(ResourceMode.ITEM, ResourceMode.XP.next());
		// a plinth loaded in essentia mode with nothing to provide it can still be clicked back to items
		assertEquals(ResourceMode.ITEM, ResourceMode.ESSENTIA.next());
		assertEquals(ResourceMode.ESSENTIA, ResourceMode.fromOrdinal(4));
		try {
			EssentiaSupport.register(new FakeEssentia());
			assertEquals(ResourceMode.ESSENTIA, ResourceMode.XP.next());
			assertEquals(ResourceMode.ITEM, ResourceMode.ESSENTIA.next());
		} finally {
			EssentiaSupport.register(null);
		}
		assertEquals(ResourceMode.ITEM, ResourceMode.XP.next());
	}

	@Test
	void sealEditNeverIndexesOutOfBounds() {
		assertEquals(SealEdit.TOGGLE_MODE, SealEdit.fromOrdinal(0));
		assertNotNull(SealEdit.fromOrdinal(-7));
		assertNotNull(SealEdit.fromOrdinal(Integer.MIN_VALUE));
		assertNotNull(SealEdit.fromOrdinal(Integer.MAX_VALUE));
	}
}
