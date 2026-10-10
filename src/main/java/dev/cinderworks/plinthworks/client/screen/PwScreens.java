package dev.cinderworks.plinthworks.client.screen;

import net.minecraft.client.gui.screens.Screen;

public final class PwScreens
{
	private PwScreens() {}

	public static boolean isOurs(Screen screen) {
		return screen instanceof PlinthScreen || screen instanceof SealScreen
				|| screen instanceof SealConfigScreen || screen instanceof NetworkOverviewScreen;
	}
}
