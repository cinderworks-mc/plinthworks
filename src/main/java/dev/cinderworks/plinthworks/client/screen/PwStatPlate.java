package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// the readout plate bolted to the upper right of the slab. it measures its own rows, so nothing can run past the border
final class PwStatPlate
{
	static final int X = 124;
	static final int Y = 15;
	static final int MIN_WIDTH = 74;
	static final int MAX_WIDTH = 100;
	private static final int PAD = 6;
	private static final int ICON_GAP = 11;
	private static final int FIRST_ROW = 53;
	private static final int ROW = 12;
	private static final ResourceLocation SPRITE = Plinthworks.id("plate");

	record Row(PwGuiTextures icon, Component value, Component tip) {}

	private Component heading = Component.empty();
	private boolean active;
	private List<Row> rows = List.of();
	private int width = MIN_WIDTH;

	// widest text a row may use before it has to be clipped
	static int textLimit() {
		return MAX_WIDTH - PAD * 2 - ICON_GAP;
	}

	void update(Font font, Component heading, boolean active, List<Row> rows) {
		this.heading = heading;
		this.active = active;
		this.rows = rows;
		int widest = font.width(heading);
		for (Row row : rows) {
			widest = Math.max(widest, font.width(row.value()));
		}
		width = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, widest + ICON_GAP + PAD * 2 - 2));
	}

	int width() {
		return width;
	}

	int height() {
		return FIRST_ROW - Y + rows.size() * ROW + 3;
	}

	void renderBg(GuiGraphics g, int ox, int oy) {
		int h = height();
		g.blitSprite(SPRITE, ox + X, oy + Y, width, h);
		PwGuiTextures.RIVET_A.render(g, ox + X + 4, oy + Y + h - 6);
		PwGuiTextures.RIVET_B.render(g, ox + X + width - 6, oy + Y + h - 6);
		// engraved line under the toolbar
		g.fill(ox + X + 3, oy + 38, ox + X + width - 3, oy + 39, 0xFF1F1E20);
		g.fill(ox + X + 3, oy + 39, ox + X + width - 3, oy + 40, 0xFF4A4744);
	}

	// coordinates are relative to the slab, called from the label pass
	void renderText(GuiGraphics g, Font font) {
		g.drawString(font, heading, X + PAD, 42, active ? PwGui.EMBER : PwGui.MUTED, false);
		int y = FIRST_ROW;
		for (Row row : rows) {
			row.icon().render(g, X + PAD, y);
			g.drawString(font, row.value(), X + PAD + ICON_GAP, y, PwGui.VALUE, false);
			y += ROW;
		}
	}

	Component tipAt(int mx, int my) {
		int y = FIRST_ROW;
		for (Row row : rows) {
			if (mx >= X + 3 && mx < X + width - 3 && my >= y - 2 && my < y + 10) {
				return row.tip();
			}
			y += ROW;
		}
		return null;
	}

	// keeps both ends of a long name and drops the middle
	static String clip(Font font, String text, int limit) {
		if (font.width(text) <= limit) {
			return text;
		}
		String gap = "...";
		int keep = text.length();
		while (keep > 2) {
			keep--;
			int head = (keep + 1) / 2;
			int tail = keep / 2;
			String candidate = text.substring(0, head) + gap + text.substring(text.length() - tail);
			if (font.width(candidate) <= limit) {
				return candidate;
			}
		}
		return gap;
	}
}
