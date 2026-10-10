package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

// shared skin bits for the screens that are not container backgrounds: colours and the stretched panel sprites
final class PwGui
{
	static final int TITLE = 0xE0C07F;
	static final int TEXT = 0xBBB6AB;
	static final int VALUE = 0xD6D1C6;
	static final int MUTED = 0x8A867F;

	private static final ResourceLocation PANEL = Plinthworks.id("panel");
	private static final ResourceLocation INSET = Plinthworks.id("inset");

	private PwGui() {}

	static void panel(GuiGraphics g, int x, int y, int w, int h) {
		g.blitSprite(PANEL, x, y, w, h);
	}

	static void inset(GuiGraphics g, int x, int y, int w, int h) {
		g.blitSprite(INSET, x, y, w, h);
	}
}
