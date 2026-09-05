package dev.hartforge.plinthworks.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public class WorkAreaPreview
{
	public static void drawStub(GuiGraphics graphics, int x, int y) {
		graphics.fill(x, y, x + 38, y + 18, 0x7f181512);
		graphics.hLine(x, x + 38, y, 0xff80664f);
		graphics.hLine(x, x + 38, y + 18, 0xff80664f);
		graphics.vLine(x, y, y + 18, 0xff80664f);
		graphics.vLine(x + 38, y, y + 18, 0xff80664f);
	}
}
