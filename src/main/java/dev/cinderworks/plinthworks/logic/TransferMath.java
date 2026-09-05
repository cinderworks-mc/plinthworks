package dev.cinderworks.plinthworks.logic;

public class TransferMath
{
	public static int moveCount(int available, int space, int batchSize) {
		return Math.max(0, Math.min(batchSize, Math.min(available, space)));
	}
}
