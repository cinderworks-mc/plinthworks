package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

// one region table for the bronze widget sheet, keeps every blit off magic coords
public enum PwGuiTextures
{
	BTN(0, 0, 16, 16),
	BTN_HOVER(16, 0, 16, 16),
	GAUGE_FRAME(32, 0, 18, 52),

	RES_ITEM(56, 0, 16, 16),
	RES_ENERGY(72, 0, 16, 16),
	RES_FLUID(88, 0, 16, 16),
	RES_XP(104, 0, 16, 16),

	TR_PRIORITY(56, 16, 16, 16),
	TR_ROUND(72, 16, 16, 16),
	TR_BALANCED(88, 16, 16, 16),
	REDSTONE(104, 16, 16, 16),
	NETWORK(56, 32, 16, 16),

	STAT_RATE(56, 48, 8, 8),
	STAT_MOVE(64, 48, 8, 8),
	STAT_HOLD(72, 48, 8, 8),
	STAT_REACH(80, 48, 8, 8),
	STAT_CHAN(88, 48, 8, 8);

	static final ResourceLocation SHEET = Plinthworks.id("textures/gui/container/plinth_widgets.png");
	static final int SHEET_SIZE = 128;

	final int u, v, w, h;

	PwGuiTextures(int u, int v, int w, int h) {
		this.u = u;
		this.v = v;
		this.w = w;
		this.h = h;
	}

	void render(GuiGraphics g, int x, int y) {
		g.blit(SHEET, x, y, u, v, w, h, SHEET_SIZE, SHEET_SIZE);
	}
}
