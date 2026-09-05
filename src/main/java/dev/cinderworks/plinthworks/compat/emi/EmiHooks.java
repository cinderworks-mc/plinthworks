package dev.cinderworks.plinthworks.compat.emi;

import org.slf4j.*;

public class EmiHooks
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EmiHooks.class);

	public static void init() {
		LOGGER.info("EMI found; plinth recipe pages are not wired yet");
	}
}
