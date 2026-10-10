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

	// foot, rim around the bowl, bowl floor and the four posts
	static <T extends ModelBuilder<T>> T fixed(T m) {
		use(m, "front", "side", "top", "inner");
		box(m, new float[]{0, 0, 0, 16, 2, 16}, 0, false, join(sides("front", "side"), sample(Direction.UP, "inner", 1, 15)));
		box(m, new float[]{0, 11, 0, 16, 14, 4}, 0, false, join(sides("front", "side"),
				face(Direction.UP, "top"), face(Direction.SOUTH, "inner"), sample(Direction.DOWN, "inner", 0, 15)));
		box(m, new float[]{0, 11, 12, 16, 14, 16}, 0, false, join(sides("front", "side"),
				face(Direction.UP, "top"), face(Direction.NORTH, "inner"), sample(Direction.DOWN, "inner", 0, 15)));
		box(m, new float[]{0, 11, 4, 4, 14, 12}, 0, false, face(Direction.WEST, "side"),
				face(Direction.UP, "top"), face(Direction.EAST, "inner"), sample(Direction.DOWN, "inner", 0, 15));
		box(m, new float[]{12, 11, 4, 16, 14, 12}, 0, false, face(Direction.EAST, "side"),
				face(Direction.UP, "top"), face(Direction.WEST, "inner"), sample(Direction.DOWN, "inner", 0, 15));
		box(m, new float[]{4, 11, 4, 12, 12, 12}, 0, false, face(Direction.UP, "top"));
		// brass band under the cornice, sticks out half a unit over the body
		box(m, new float[]{1.5F, 10.5F, 1.5F, 14.5F, 11, 14.5F}, 0, false, join(sides("front", "side"),
				sample(Direction.DOWN, "front", 14, 5)));
		// hearth lintel and sill stand proud of the front and back faces
		for (boolean north : new boolean[]{true, false}) {
			Direction out = north ? Direction.NORTH : Direction.SOUTH;
			float z0 = north ? 1.5F : 14;
			float z1 = north ? 2 : 14.5F;
			box(m, new float[]{5, 8, z0, 11, 9, z1}, 0, false, face(out, "front"), sample(Direction.EAST, "front", 10, 7),
					sample(Direction.WEST, "front", 10, 7), sample(Direction.UP, "front", 5, 7), sample(Direction.DOWN, "front", 9, 7));
			box(m, new float[]{5, 4, z0, 11, 5, z1}, 0, false, face(out, "front"), sample(Direction.EAST, "front", 10, 11),
					sample(Direction.WEST, "front", 10, 11), sample(Direction.UP, "front", 6, 11), sample(Direction.DOWN, "front", 9, 11));
		}
		// posts: a stone shaft under a brass cap that leans half a unit in over the bowl
		for (int px : new int[]{0, 12}) {
			for (int pz : new int[]{0, 12}) {
				box(m, new float[]{px, 14, pz, px + 4, 15, pz + 4}, 0, false, sides("front", "side"));
				float x0 = px == 0 ? 0 : 11.5F;
				float x1 = px == 0 ? 4.5F : 16;
				float z0 = pz == 0 ? 0 : 11.5F;
				float z1 = pz == 0 ? 4.5F : 16;
				float[] edge = {0, 0, 4, 1};
				box(m, new float[]{x0, 15, z0, x1, 16, z1}, 0, false,
						new Face(Direction.NORTH, "front", edge), new Face(Direction.SOUTH, "front", edge),
						new Face(Direction.EAST, "side", edge), new Face(Direction.WEST, "side", edge),
						new Face(Direction.UP, "top", new float[]{px, pz, px + 4, pz + 4}),
						sample(Direction.DOWN, "inner", 0, 15));
			}
		}
		return m;
	}

	// trim drawn over the worn block's body, empty pixels let the block show through
	static <T extends ModelBuilder<T>> T worn(T m) {
		use(m, "front", "mode", "gauge");
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

	// lit grooves, hearth mouth and coal bed while the plinth works
	static <T extends ModelBuilder<T>> T glow(T m) {
		use(m, "glow_front", "glow_side", "glow_bowl", "glow_top");
		box(m, new float[]{2, 4, 2, 14, 11, 14}, GLOW_LIFT, true,
				face(Direction.NORTH, "glow_front"), face(Direction.SOUTH, "glow_front"),
				face(Direction.EAST, "glow_side"), face(Direction.WEST, "glow_side"));
		box(m, new float[]{0, 14.05F, 0, 16, 14.05F, 16}, 0, true, face(Direction.UP, "glow_top"));
		box(m, new float[]{4, 12.04F, 4, 12, 12.04F, 12}, 0, true,
				new Face(Direction.UP, "glow_bowl", new float[]{4, 4, 12, 12}));
		return m;
	}

	// the mode glyph, a 6x5 cell on the east face panel
	static <T extends ModelBuilder<T>> T glyph(T m, int mode) {
		use(m, "glyphs");
		String[] cell = GLYPH_CELL[mode].split(",");
		float u = Integer.parseInt(cell[0]);
		float v = Integer.parseInt(cell[1]);
		box(m, new float[]{14 + PANEL_LIFT, 4, 5, 14 + PANEL_LIFT, 9, 11}, 0, false,
				new Face(Direction.EAST, "glyphs", new float[]{u, v, u + 6, v + 5}));
		return m;
	}

	// lit gauge bars on the west face, the bar at column 3 + 2i is lit from level i on
	static <T extends ModelBuilder<T>> T gauge(T m, int level) {
		use(m, "glyphs");
		for (int i = 1; i <= level; i++) {
			int col = 3 + 2 * i;
			box(m, new float[]{2 - PANEL_LIFT, 4, col, 2 - PANEL_LIFT, 8, col + 1}, 0, false,
					new Face(Direction.WEST, "glyphs", new float[]{9 + i, 12, 10 + i, 16}));
		}
		return m;
	}
}
