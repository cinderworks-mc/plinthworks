package dev.cinderworks.plinthworks.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

// the baked slab for the two small screens, plus the plain wells their widgets sit in
final class PwFrame
{
	static final int WIDTH = 196;
	static final int HEIGHT = 152;
	private static final ResourceLocation TEXTURE = Plinthworks.id("textures/gui/container/frame.png");

	private PwFrame() {}

	static void render(GuiGraphics g, int x, int y) {
		RenderSystem.enableBlend();
		g.blit(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT, 256, 256);
	}

	// char well, dark edge on the top and left, light lip on the bottom and right
	static void well(GuiGraphics g, int x, int y, int w, int h) {
		g.fill(x, y, x + w, y + h, 0xFF1F1E20);
		g.fill(x, y, x + w, y + 1, 0xFF0E0D0F);
		g.fill(x, y, x + 1, y + h, 0xFF0E0D0F);
		g.fill(x + 1, y + h, x + w + 1, y + h + 1, 0xFF4A4744);
		g.fill(x + w, y + 1, x + w + 1, y + h + 1, 0xFF4A4744);
	}
}
