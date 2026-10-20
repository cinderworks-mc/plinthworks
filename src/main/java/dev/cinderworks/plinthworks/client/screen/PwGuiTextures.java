package dev.cinderworks.plinthworks.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

// one region table for the stone widget sheet, keeps every blit off magic coords
public enum PwGuiTextures
{
	BTN(0, 0, 16, 16),
	BTN_HOVER(16, 0, 16, 16),
	BTN_PRESSED(32, 0, 16, 16),
	BTN_TOGGLED(48, 0, 16, 16),
	BTN_DISABLED(64, 0, 16, 16),
	BTN_FOCUS(80, 0, 20, 20),

	GAUGE_FRAME(0, 24, 23, 52),
	GAUGE_GLASS(24, 24, 12, 44),
	GAUGE_HATCH(38, 24, 12, 44),
	CAP_ENERGY(52, 24, 7, 5),
	CAP_FLUID(60, 24, 7, 5),
	CAP_XP(68, 24, 7, 5),
	CAP_ESSENTIA(76, 24, 7, 5),

	RES_ITEM(0, 80, 16, 16),
	RES_ENERGY(16, 80, 16, 16),
	RES_FLUID(32, 80, 16, 16),
	RES_XP(48, 80, 16, 16),
	TR_PRIORITY(64, 80, 16, 16),
	TR_ROUND(80, 80, 16, 16),
	TR_BALANCED(96, 80, 16, 16),
	REDSTONE(112, 80, 16, 16),
	NETWORK(128, 80, 16, 16),
	RES_ESSENTIA(144, 80, 16, 16),

	STAT_RATE(0, 100, 8, 8),
	STAT_MOVE(8, 100, 8, 8),
	STAT_HOLD(16, 100, 8, 8),
	STAT_REACH(24, 100, 8, 8),
	STAT_CHAN(32, 100, 8, 8),

	SOCKET_LIT(0, 112, 22, 22),
	TAG(24, 112, 14, 11),
	TAG_HOVER(38, 112, 14, 11),
	TAG_PRESSED(52, 112, 14, 11),
	HINT_ETCH(68, 112, 6, 7),
	HINT_SEAL(76, 112, 8, 8),
	HINT_SIGIL(86, 112, 10, 10),
	RIVET_A(98, 112, 2, 2),
	RIVET_B(101, 112, 2, 2),
	PLUS(106, 112, 7, 7);

	static final ResourceLocation SHEET = Plinthworks.id("textures/gui/container/plinth_widgets.png");
	static final int SHEET_W = 256;
	static final int SHEET_H = 160;

	final int u, v, w, h;

	PwGuiTextures(int u, int v, int w, int h) {
		this.u = u;
		this.v = v;
		this.w = w;
		this.h = h;
	}

	void render(GuiGraphics g, int x, int y) {
		// the sheet has soft edged pixels, and the gui does not always leave blending on
		RenderSystem.enableBlend();
		g.blit(SHEET, x, y, u, v, w, h, SHEET_W, SHEET_H);
	}
}
