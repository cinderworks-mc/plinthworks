package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.item.*;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.*;

class UpgradeSetTest
{
	@Test
	void derivesStatsFromEtchingLevels() {
		EnumMap<EtchingType, Integer> levels = new EnumMap<>(EtchingType.class);
		UpgradeSet base = UpgradeSet.fromLevels(null, false, levels);
		assertNull(base.verb());
		assertEquals(20, base.intervalTicks());
		assertEquals(1, base.throughput());
		assertEquals(1, base.bufferSlots());
		assertEquals(1, base.range());

		levels.put(EtchingType.SPEED, 4);
		levels.put(EtchingType.CAPACITY, 4);
		levels.put(EtchingType.STORAGE, 4);
		levels.put(EtchingType.RANGE, 4);
		UpgradeSet upgraded = UpgradeSet.fromLevels(SigilType.IMPORT, true, levels);
		assertEquals(SigilType.IMPORT, upgraded.verb());
		assertTrue(upgraded.filtered());
		assertEquals(8, upgraded.intervalTicks());
		assertEquals(33, upgraded.throughput());
		assertEquals(5, upgraded.bufferSlots());
		assertEquals(5, upgraded.range());
	}
}
