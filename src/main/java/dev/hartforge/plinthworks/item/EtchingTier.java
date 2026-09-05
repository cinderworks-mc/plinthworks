package dev.hartforge.plinthworks.item;

public enum EtchingTier
{
	T1(1),
	T2(2),
	T3(3),
	T4(4);

	private final int level;

	EtchingTier(int level) {
		this.level = level;
	}

	public int level() {
		return level;
	}
}
