package dev.cinderworks.plinthworks.compat;

import dev.cinderworks.plinthworks.compat.create.CreateHooks;
import dev.cinderworks.plinthworks.compat.emi.EmiHooks;
import dev.cinderworks.plinthworks.compat.jei.JeiHooks;

public class CompatHooks
{
	public static void init() {
		if (ModIds.loaded(ModIds.CREATE)) {
			CreateHooks.init();
		}
		if (ModIds.loaded(ModIds.JEI)) {
			JeiHooks.init();
		}
		if (ModIds.loaded(ModIds.EMI)) {
			EmiHooks.init();
		}
		// TODO: an ME bridge can become a network access point later
	}
}
