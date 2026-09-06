package dev.cinderworks.plinthworks.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferMathTest
{
	@Test
	void clampsToBatchAndSpace() {
		assertEquals(4, TransferMath.moveCount(12, 10, 4));
		assertEquals(2, TransferMath.moveCount(12, 2, 4));
		assertEquals(0, TransferMath.moveCount(12, 0, 4));
	}
}
