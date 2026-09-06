package dev.cinderworks.plinthworks.logic.network;

import net.minecraft.core.BlockPos;

import java.util.List;

public record NetworkChannel(String name, List<BlockPos> members, TransferMode mode)
{
	public NetworkChannel {
		members = List.copyOf(members);
	}
}
