package dev.cinderworks.plinthworks.logic;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.*;

import java.util.UUID;

public class PlinthFakePlayer
{
	private static final GameProfile PROFILE = new GameProfile(
			UUID.fromString("6dfab3ca-cfb7-4f73-a377-5f03d84747dc"), "[plinthworks]");

	public static FakePlayer get(ServerLevel level, BlockPos pos) {
		FakePlayer player = FakePlayerFactory.get(level, PROFILE);
		player.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		return player;
	}
}
