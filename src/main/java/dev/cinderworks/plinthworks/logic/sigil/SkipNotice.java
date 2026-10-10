package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.item.SigilType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

// action bar line for players standing near a plinth whose world verb skipped a block
public final class SkipNotice
{
	public static final int COOLDOWN_TICKS = 200;
	private static final double RADIUS_SQ = 8 * 8;

	public enum Reason
	{
		FULL,
		VETO,
		FAILED
	}

	private SkipNotice() {}

	public static void report(PlinthBlockEntity be, ServerLevel level, Reason reason) {
		SigilType verb = be.upgrades().verb();
		long now = level.getGameTime();
		if (verb == null || now < be.nextSkipNotice() || !ModConfig.SKIP_MESSAGES.get()) {
			return;
		}
		// the timer runs even with nobody near, so an idle plinth costs one comparison per skip
		be.setNextSkipNotice(now + COOLDOWN_TICKS);
		BlockPos pos = be.getBlockPos();
		Component text = null;
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= RADIUS_SQ) {
				if (text == null) {
					text = Component.translatable("message.plinthworks.skip."
							+ reason.name().toLowerCase(Locale.ROOT) + "." + verb.name().toLowerCase(Locale.ROOT));
				}
				player.displayClientMessage(text, true);
			}
		}
	}
}
