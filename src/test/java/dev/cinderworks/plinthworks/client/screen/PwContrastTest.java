package dev.cinderworks.plinthworks.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

// every text colour against the surfaces it is drawn on, 4.5 for body text
class PwContrastTest
{
	private static final int PATCH = 0x3F3D3B;
	private static final int WELL = 0x1F1E20;

	private static void atLeast(double ratio, int foreground, int background) {
		double actual = PwGui.contrast(foreground, background);
		assertTrue(actual >= ratio,
				String.format("%06X on %06X is %.2f, wants %.1f", foreground, background, actual, ratio));
	}

	@Test
	void labelsReadOnTheSlab() {
		atLeast(4.5, PwGui.LABEL, PwGui.SLAB_MID);
		atLeast(4.5, PwGui.LABEL, PwGui.SLAB);
		atLeast(4.5, PwGui.LABEL, PwGui.FIELD);
	}

	@Test
	void quietTextReadsOnSlabAndField() {
		atLeast(4.5, PwGui.MUTED, PwGui.SLAB_MID);
		atLeast(4.5, PwGui.MUTED, PATCH);
		atLeast(4.5, PwGui.MUTED, PwGui.FIELD);
		atLeast(4.5, PwGui.MUTED, PwGui.PLATE);
	}

	@Test
	void valuesAndHeadingsReadOnTheDarkSurfaces() {
		atLeast(4.5, PwGui.VALUE, PwGui.PLATE);
		atLeast(4.5, PwGui.VALUE, WELL);
		atLeast(4.5, PwGui.TITLE, PwGui.PLATE);
		atLeast(4.5, PwGui.TITLE, PwGui.SLAB_MID);
		atLeast(4.5, PwGui.EMBER, PwGui.PLATE);
		atLeast(4.5, PwGui.BRASS, PwGui.PLATE);
		atLeast(4.5, PwGui.BRASS, PwGui.SLAB_MID);
	}

	// the lit verb heading is the one ember line of text, plain ember only made 5.8 there and looked dim
	@Test
	void emberHeadingReadsOnThePlate() {
		atLeast(7.0, PwStatPlate.headingColor(true), PwGui.PLATE);
		atLeast(4.5, PwStatPlate.headingColor(false), PwGui.PLATE);
	}
}
