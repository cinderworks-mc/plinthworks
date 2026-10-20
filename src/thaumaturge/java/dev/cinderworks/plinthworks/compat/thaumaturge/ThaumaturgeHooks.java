package dev.cinderworks.plinthworks.compat.thaumaturge;

import dev.cinderworks.plinthworks.logic.resource.EssentiaSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThaumaturgeHooks
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ThaumaturgeHooks.class);

	public static void init() {
		EssentiaSupport.register(new ThaumaturgeEssentia());
		LOGGER.info("Thaumaturge found; plinths can move essentia");
	}
}
