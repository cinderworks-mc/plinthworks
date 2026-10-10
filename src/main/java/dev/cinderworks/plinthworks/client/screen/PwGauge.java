package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.client.PwMotion;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

// brass tube with an eased fill. energy is ember, xp is brass, fluid is its own still sprite
final class PwGauge
{
	static final int WIDTH = 23;
	static final int HEIGHT = 52;
	private static final int INNER_X = 3;
	private static final int INNER_Y = 4;
	private static final int INNER_W = 12;
	private static final int INNER_H = 44;

	private static final int EMBER = 0xFFC75A1E;
	private static final int EMBER_BRIGHT = 0xFFF08A3C;
	private static final int EMBER_HOT = 0xFFFFC070;
	private static final int BRASS = 0xFFD2A55A;
	private static final int BRASS_SHINE = 0xFFE8C27A;
	private static final int FLUID_FALLBACK = 0xFF8A867F;

	// fixed spots for the xp sparkles, measured from the top of the glass
	private static final int[][] SPARKS = {{2, 6}, {8, 15}, {4, 27}, {9, 36}};

	private float shown;
	private float previous;
	private ResourceMode mode;

	void tick(ResourceMode current, float target) {
		if (current != mode || PwMotion.reduced()) {
			mode = current;
			shown = target;
			previous = target;
			return;
		}
		previous = shown;
		shown += (target - shown) * 0.25F;
		if (Math.abs(target - shown) < 0.004F) {
			shown = target;
		}
	}

	boolean empty() {
		return shown <= 0F;
	}

	void render(GuiGraphics g, int x, int y, ResourceMode current, FluidStack fluid, float partial) {
		PwGuiTextures.GAUGE_FRAME.render(g, x, y);
		int gx = x + INNER_X;
		int bottom = y + INNER_Y + INNER_H;
		float frac = Mth.lerp(partial, previous, shown);
		int fill = Math.round(frac * INNER_H);
		if (frac > 0F && fill == 0) {
			fill = 1;
		}
		if (fill > 0) {
			int top = bottom - fill;
			switch (current) {
				case ENERGY -> fillEnergy(g, gx, top, bottom);
				case FLUID -> fillFluid(g, gx, top, bottom, fluid);
				default -> fillXp(g, gx, top, bottom, y + INNER_Y);
			}
		} else {
			PwGuiTextures.GAUGE_HATCH.render(g, gx, y + INNER_Y);
		}
		PwGuiTextures.GAUGE_GLASS.render(g, gx, y + INNER_Y);
	}

	// ember at the bottom, a brighter band above it and a one pixel hot meniscus
	private static void fillEnergy(GuiGraphics g, int gx, int top, int bottom) {
		int height = bottom - top;
		int split = bottom - Math.max(1, height * 55 / 100);
		g.fill(gx, top, gx + INNER_W, split, EMBER_BRIGHT);
		g.fill(gx, split, gx + INNER_W, bottom, EMBER);
		g.fill(gx, top, gx + INNER_W, top + 1, EMBER_HOT);
	}

	private static void fillXp(GuiGraphics g, int gx, int top, int bottom, int glassTop) {
		g.fill(gx, top, gx + INNER_W, bottom, BRASS);
		g.fill(gx, top, gx + INNER_W, top + 1, BRASS_SHINE);
		for (int[] spark : SPARKS) {
			int sy = glassTop + spark[1];
			if (sy > top) {
				g.fill(gx + spark[0], sy, gx + spark[0] + 1, sy + 1, BRASS_SHINE);
			}
		}
	}

	// tile the fluid's own still sprite up the fill, clipped to the filled height
	private static void fillFluid(GuiGraphics g, int gx, int top, int bottom, FluidStack fluid) {
		if (fluid.isEmpty()) {
			g.fill(gx, top, gx + INNER_W, bottom, FLUID_FALLBACK);
			return;
		}
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
		TextureAtlasSprite sprite = Minecraft.getInstance()
				.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fluid));
		int tint = ext.getTintColor(fluid);
		g.setColor(((tint >> 16) & 0xFF) / 255f, ((tint >> 8) & 0xFF) / 255f, (tint & 0xFF) / 255f, 1f);
		g.enableScissor(gx, top, gx + INNER_W, bottom);
		for (int y = bottom - 16; y + 16 > top; y -= 16) {
			g.blit(gx, y, 0, INNER_W, 16, sprite);
		}
		g.disableScissor();
		g.setColor(1f, 1f, 1f, 1f);
		// lighter meniscus row
		g.fill(gx, top, gx + INNER_W, top + 1, 0x59FFFFFF);
	}
}
