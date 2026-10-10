package dev.cinderworks.plinthworks.client.screen;

// text and fill colours shared by the screens, see the contrast test for the pairs that must stay readable
final class PwGui
{
	static final int TITLE = 0xE0C07F;
	static final int LABEL = 0xCFC8B8;
	static final int VALUE = 0xE6E0D2;
	static final int MUTED = 0xB0AA9E;
	static final int BRASS = 0xD2A55A;
	static final int EMBER = 0xF08A3C;

	static final int SLAB_MID = 0x3A3836;
	static final int SLAB = 0x4A4744;
	static final int FIELD = 0x353331;
	static final int PLATE = 0x2B2927;

	private PwGui() {}

	static double luminance(int rgb) {
		return 0.2126 * channel(rgb >> 16) + 0.7152 * channel(rgb >> 8) + 0.0722 * channel(rgb);
	}

	private static double channel(int value) {
		double c = (value & 0xFF) / 255.0;
		return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}

	static double contrast(int foreground, int background) {
		double a = luminance(foreground);
		double b = luminance(background);
		return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
	}
}
