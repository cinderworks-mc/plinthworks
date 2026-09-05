package dev.hartforge.plinthworks.logic;

import dev.hartforge.plinthworks.item.*;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EtchingMathTest
{
	@Test
	void capacityAndStorageStayIndependent() {
		EnumMap<EtchingType, Integer> levels = new EnumMap<>(EtchingType.class);
		levels.put(EtchingType.CAPACITY, 2);
		UpgradeSet capacity = UpgradeSet.fromLevels(SigilType.EXPORT, false, levels);
		assertEquals(17, capacity.throughput());
		assertEquals(1, capacity.bufferSlots());

		levels.clear();
		levels.put(EtchingType.STORAGE, 2);
		UpgradeSet storage = UpgradeSet.fromLevels(SigilType.EXPORT, false, levels);
		assertEquals(1, storage.throughput());
		assertEquals(3, storage.bufferSlots());
	}
}
