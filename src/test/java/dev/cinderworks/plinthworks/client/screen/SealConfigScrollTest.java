package dev.cinderworks.plinthworks.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// the key list used to stop at four, every key has to land inside the window at some scroll
class SealConfigScrollTest
{
	@Test
	void everyKeyCanBeScrolledIntoView() {
		for (int count = 0; count <= 70; count++) {
			int last = SealConfigScreen.clampScroll(Integer.MAX_VALUE, count);
			for (int key = 0; key < count; key++) {
				int k = key;
				boolean reachable = java.util.stream.IntStream.rangeClosed(0, last)
						.anyMatch(scroll -> k >= scroll && k < scroll + SealConfigScreen.VISIBLE);
				assertTrue(reachable, "key " + key + " of " + count + " never shows");
			}
		}
	}

	@Test
	void scrollStaysInsideTheList() {
		assertEquals(0, SealConfigScreen.clampScroll(-3, 10));
		assertEquals(0, SealConfigScreen.clampScroll(5, 3));
		assertEquals(6, SealConfigScreen.clampScroll(99, 10));
	}
}
