package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.client.PwMotion;
import net.minecraft.client.gui.GuiGraphics;

// the line from the sigil socket to the etching rail and down to the seals. the idle line is part of the background texture
final class PwThread
{
	private static final int[][] PATH = {{29, 37}, {48, 37}, {48, 47}, {59, 47}, {59, 74}, {60, 74}};
	private static final int LIT = 0xFFC75A1E;
	private static final int PULSE = 0xFFFFC070;
	private static final int LOOP = 32;
	private static final int[] XS;
	private static final int[] YS;

	static {
		int count = 1;
		for (int i = 1; i < PATH.length; i++) {
			count += Math.abs(PATH[i][0] - PATH[i - 1][0]) + Math.abs(PATH[i][1] - PATH[i - 1][1]);
		}
		XS = new int[count];
		YS = new int[count];
		int n = 0;
		XS[0] = PATH[0][0];
		YS[0] = PATH[0][1];
		for (int i = 1; i < PATH.length; i++) {
			int dx = Integer.signum(PATH[i][0] - PATH[i - 1][0]);
			int dy = Integer.signum(PATH[i][1] - PATH[i - 1][1]);
			int steps = Math.abs(PATH[i][0] - PATH[i - 1][0]) + Math.abs(PATH[i][1] - PATH[i - 1][1]);
			for (int s = 0; s < steps; s++) {
				n++;
				XS[n] = XS[n - 1] + dx;
				YS[n] = YS[n - 1] + dy;
			}
		}
	}

	private PwThread() {}

	static void renderLit(GuiGraphics g, int ox, int oy, int age) {
		for (int i = 0; i < XS.length; i++) {
			g.fill(ox + XS[i], oy + YS[i], ox + XS[i] + 1, oy + YS[i] + 1, LIT);
		}
		if (PwMotion.reduced()) {
			return;
		}
		for (int i = age % LOOP; i < XS.length; i += LOOP) {
			g.fill(ox + XS[i], oy + YS[i], ox + XS[i] + 1, oy + YS[i] + 1, PULSE);
		}
	}
}
