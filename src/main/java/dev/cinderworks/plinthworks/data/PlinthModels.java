package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

// the plinth is built in layers so the worn block can show through: fixed stone and brass, a trim layer over the
// worn body, glow while active, and a glyph and gauge on the east and west faces. every face samples its texture in
// world space (front and side are elevations, top is the plan), so uvs come from the unlifted box coordinates
final class PlinthModels
{
	// decals sit just proud of the box they cover so they never fight with it
	private static final float TRIM_LIFT = 0.03F;
	private static final float GLOW_LIFT = 0.06F;
	private static final float PANEL_LIFT = 0.09F;

	// where the mound sides live in the inner and glow_mound textures
	private static final float[] MOUND_LOW = {0, 5, 10, 7};
	private static final float[] CROWN_FRONT = {0, 8, 4, 10};
	private static final float[] CROWN_SIDE = {0, 10, 6, 12};

	private static final String[] GLYPH_CELL = {"0,0", "8,0", "0,5", "8,5"};

	private PlinthModels() {}

	static ModelFile.UncheckedModelFile block() {
		return new ModelFile.UncheckedModelFile(ResourceLocation.withDefaultNamespace("block/block"));
	}

	private static ResourceLocation tex(String name) {
		return Plinthworks.id("block/plinth_" + name);
	}

	private static float[] uv(Direction dir, float x0, float y0, float z0, float x1, float y1, float z1) {
		return switch (dir) {
			case DOWN -> new float[]{x0, 16 - z1, x1, 16 - z0};
			case UP -> new float[]{x0, z0, x1, z1};
			case NORTH -> new float[]{16 - x1, 16 - y1, 16 - x0, 16 - y0};
			case SOUTH -> new float[]{x0, 16 - y1, x1, 16 - y0};
			case WEST -> new float[]{z0, 16 - y1, z1, 16 - y0};
			case EAST -> new float[]{16 - z1, 16 - y1, 16 - z0, 16 - y0};
		};
	}

	private record Face(Direction dir, String texture, float[] uv) {}

	private static Face face(Direction dir, String texture) {
		return new Face(dir, texture, null);
	}

	// one texel of a texture stretched over a whole face, for flat ledges and shadowed undersides
	private static Face sample(Direction dir, String texture, int u, int v) {
		return new Face(dir, texture, new float[]{u, v, u + 1, v + 1});
	}

	private static Face[] sides(String front, String side) {
		return new Face[]{face(Direction.NORTH, front), face(Direction.SOUTH, front),
				face(Direction.EAST, side), face(Direction.WEST, side)};
	}

	private static <T extends ModelBuilder<T>> void box(T model, float[] b, float grow, boolean glow, Face... faces) {
		ModelBuilder<T>.ElementBuilder element = model.element()
				.from(b[0] - grow, b[1], b[2] - grow).to(b[3] + grow, b[4], b[5] + grow);
		if (glow) {
			element.shade(false).ao(false).emissivity(15, 0);
		}
		for (Face f : faces) {
			float[] uv = f.uv != null ? f.uv : uv(f.dir, b[0], b[1], b[2], b[3], b[4], b[5]);
			element.face(f.dir).texture("#" + f.texture).uvs(uv[0], uv[1], uv[2], uv[3]);
		}
		element.end();
	}

	private static Face[] join(Face[] a, Face... b) {
		Face[] out = java.util.Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}

	private static <T extends ModelBuilder<T>> T use(T model, String... names) {
		for (String n : names) {
			model.texture(n, tex(n));
		}
		return model.texture("particle", tex("stone")).renderType("cutout");
	}

	// foot, cap, the two brass flanking blocks, the tray posts and the coal mound in the bowl
	static <T extends ModelBuilder<T>> T fixed(T m) {
		use(m, "front", "side", "top", "inner");
		// charcoal foot, the worn block draws the step and the body
		box(m, new float[]{0, 0, 0, 16, 2, 16}, 0, false, join(sides("front", "side"),
				sample(Direction.UP, "inner", 1, 15), sample(Direction.DOWN, "inner", 0, 15)));
		// cap overhangs the body by two units all round
		box(m, new float[]{0, 11, 0, 16, 13, 16}, 0, false, join(sides("front", "side"),
				face(Direction.UP, "top"), sample(Direction.DOWN, "inner", 0, 15)));
		// tray posts at the four corners of the bowl, the gap between each pair shows the mound
		for (float[] post : new float[][]{{5, 0}, {9, 0}, {5, 14}, {9, 14}}) {
			box(m, new float[]{post[0], 13, post[1], post[0] + 2, 15, post[1] + 2}, 0, false,
					join(sides("front", "side"), face(Direction.UP, "top")));
		}
		// brass flanking blocks, the outer faces carry the relief and the inner faces stay plain
		box(m, new float[]{0, 13, 5, 5, 16, 11}, 0, false, face(Direction.NORTH, "front"), face(Direction.SOUTH, "front"),
				face(Direction.WEST, "side"), sample(Direction.EAST, "inner", 4, 15), face(Direction.UP, "top"));
		box(m, new float[]{11, 13, 5, 16, 16, 11}, 0, false, face(Direction.NORTH, "front"), face(Direction.SOUTH, "front"),
				face(Direction.EAST, "side"), sample(Direction.WEST, "inner", 4, 15), face(Direction.UP, "top"));
		// coal mound, a wide course and a crown that rises over the flanks
		box(m, new float[]{5, 13, 3, 11, 15, 13}, 0, false, face(Direction.NORTH, "front"), face(Direction.SOUTH, "front"),
				new Face(Direction.EAST, "inner", MOUND_LOW), new Face(Direction.WEST, "inner", MOUND_LOW),
				face(Direction.UP, "top"));
		box(m, new float[]{6, 15, 5, 10, 17, 11}, 0, false, new Face(Direction.NORTH, "inner", CROWN_FRONT),
				new Face(Direction.SOUTH, "inner", CROWN_FRONT), new Face(Direction.EAST, "inner", CROWN_SIDE),
				new Face(Direction.WEST, "inner", CROWN_SIDE), face(Direction.UP, "top"));
		return m;
	}

	// trim drawn over the worn block: a contact line on the step, carved pillars, hearth frame and the side panels
	static <T extends ModelBuilder<T>> T worn(T m) {
		use(m, "front", "mode", "gauge");
		box(m, new float[]{1, 2, 1, 15, 4, 15}, TRIM_LIFT, false, face(Direction.NORTH, "front"),
				face(Direction.SOUTH, "front"), face(Direction.EAST, "mode"), face(Direction.WEST, "gauge"));
		box(m, new float[]{2, 4, 2, 14, 11, 14}, TRIM_LIFT, false, face(Direction.NORTH, "front"),
				face(Direction.SOUTH, "front"), face(Direction.EAST, "mode"), face(Direction.WEST, "gauge"));
		return m;
	}

	// stand in body for the inventory model, where there is no worn block to show
	static <T extends ModelBuilder<T>> T stoneBody(T m) {
		use(m, "stone");
		box(m, new float[]{1, 2, 1, 15, 4, 15}, 0, false, sides("stone", "stone"));
		box(m, new float[]{2, 4, 2, 14, 11, 14}, 0, false, sides("stone", "stone"));
		return m;
	}

	// hearth, sigil stems, the cap grooves and the crown of the mound light up while the plinth works
	static <T extends ModelBuilder<T>> T glow(T m) {
		use(m, "glow_front", "glow_side", "glow_bowl", "glow_top", "glow_mound");
		box(m, new float[]{2, 4, 2, 14, 11, 14}, GLOW_LIFT, true,
				face(Direction.NORTH, "glow_front"), face(Direction.SOUTH, "glow_front"),
				face(Direction.EAST, "glow_side"), face(Direction.WEST, "glow_side"));
		box(m, new float[]{0, 11, 0, 16, 13, 16}, GLOW_LIFT, true, face(Direction.NORTH, "glow_front"),
				face(Direction.SOUTH, "glow_front"), face(Direction.EAST, "glow_side"), face(Direction.WEST, "glow_side"));
		box(m, new float[]{5, 13, 3, 11, 15.04F, 13}, 0.03F, true,
				face(Direction.NORTH, "glow_front"), face(Direction.SOUTH, "glow_front"),
				new Face(Direction.EAST, "glow_mound", MOUND_LOW), new Face(Direction.WEST, "glow_mound", MOUND_LOW),
				face(Direction.UP, "glow_bowl"));
		box(m, new float[]{6, 15.04F, 5, 10, 17.04F, 11}, 0.03F, true, new Face(Direction.NORTH, "glow_mound", CROWN_FRONT),
				new Face(Direction.SOUTH, "glow_mound", CROWN_FRONT), new Face(Direction.EAST, "glow_mound", CROWN_SIDE),
				new Face(Direction.WEST, "glow_mound", CROWN_SIDE), face(Direction.UP, "glow_bowl"));
		box(m, new float[]{7, 13.04F, 0, 9, 13.04F, 2}, 0, true, face(Direction.UP, "glow_top"));
		box(m, new float[]{7, 13.04F, 14, 9, 13.04F, 16}, 0, true, face(Direction.UP, "glow_top"));
		return m;
	}

	// the mode glyph, a 6x5 cell on the east face panel
	static <T extends ModelBuilder<T>> T glyph(T m, int mode) {
		use(m, "glyphs");
		String[] cell = GLYPH_CELL[mode].split(",");
		float u = Integer.parseInt(cell[0]);
		float v = Integer.parseInt(cell[1]);
		box(m, new float[]{14 + PANEL_LIFT, 5, 5, 14 + PANEL_LIFT, 10, 11}, 0, false,
				new Face(Direction.EAST, "glyphs", new float[]{u, v, u + 6, v + 5}));
		return m;
	}

	// lit gauge bars on the west face, the bar at column 5 + i is lit from level i on
	static <T extends ModelBuilder<T>> T gauge(T m, int level) {
		use(m, "glyphs");
		for (int i = 1; i <= level; i++) {
			int col = 5 + i;
			box(m, new float[]{2 - PANEL_LIFT, 5, col, 2 - PANEL_LIFT, 10, col + 1}, 0, false,
					new Face(Direction.WEST, "glyphs", new float[]{9 + i, 11, 10 + i, 16}));
		}
		return m;
	}
}
