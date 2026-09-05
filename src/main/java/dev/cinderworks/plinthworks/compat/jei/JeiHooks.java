package dev.cinderworks.plinthworks.compat.jei;

import org.slf4j.*;

public class JeiHooks
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JeiHooks.class);

	public static void init() {
		LOGGER.info("JEI found; plinth recipe pages are not wired yet");
	}
}
