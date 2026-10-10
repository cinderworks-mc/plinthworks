package dev.cinderworks.plinthworks.client;

import dev.cinderworks.plinthworks.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;

// one switch for everything that moves on a screen or in the world
public final class PwMotion
{
	private PwMotion() {}

	public static boolean reduced() {
		if (ClientConfig.REDUCED_MOTION.get()) {
			return true;
		}
		// minimal particles counts as asking for less motion too
		return Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL;
	}
}
