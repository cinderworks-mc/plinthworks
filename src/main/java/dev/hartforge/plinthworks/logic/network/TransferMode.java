package dev.hartforge.plinthworks.logic.network;

public enum TransferMode
{
	PRIORITY,
	ROUND_ROBIN,
	LOAD_BALANCED;

	public TransferMode next() {
		return values()[(ordinal() + 1) % values().length];
	}
}
