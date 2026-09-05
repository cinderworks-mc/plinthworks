package dev.hartforge.plinthworks.compat.create;

import org.slf4j.*;

public class CreateHooks
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateHooks.class);

	public static void init() {
		LOGGER.info("Create found; kinetic plinth support is not wired yet");
	}
}
