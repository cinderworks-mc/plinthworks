package dev.cinderworks.plinthworks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig
{
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	public static final ModConfigSpec.BooleanValue REDUCED_MOTION;
	public static final ModConfigSpec SPEC;

	static {
		BUILDER.push("motion");
		REDUCED_MOTION = BUILDER
				.comment("Turns off the gauge easing and the moving glow on screens.")
				.define("reducedMotion", false);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
