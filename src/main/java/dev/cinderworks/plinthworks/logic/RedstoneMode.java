package dev.cinderworks.plinthworks.logic;

public enum RedstoneMode
{
	ALWAYS,
	PAUSE_WHEN_POWERED,
	RUN_WHEN_POWERED;

	public RedstoneMode next() {
		return values()[(ordinal() + 1) % values().length];
	}

	// same as ResourceMode, a negative stored int must not go out of bounds
	public static RedstoneMode fromOrdinal(int ordinal) {
		return values()[Math.floorMod(ordinal, values().length)];
	}

	public boolean allows(boolean powered) {
		return switch (this) {
			case ALWAYS -> true;
			case PAUSE_WHEN_POWERED -> !powered;
			case RUN_WHEN_POWERED -> powered;
		};
	}
}
