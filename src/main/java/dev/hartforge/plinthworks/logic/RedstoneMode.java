package dev.hartforge.plinthworks.logic;

public enum RedstoneMode
{
	ALWAYS,
	PAUSE_WHEN_POWERED,
	RUN_WHEN_POWERED;

	public RedstoneMode next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public boolean allows(boolean powered) {
		return switch (this) {
			case ALWAYS -> true;
			case PAUSE_WHEN_POWERED -> !powered;
			case RUN_WHEN_POWERED -> powered;
		};
	}
}
