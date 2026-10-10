package dev.cinderworks.plinthworks.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

// the numbered tab that hangs under a seal plate
class PwSealTag extends AbstractButton
{
	private final Runnable onPress;
	private boolean down;

	PwSealTag(int x, int y, int number, Runnable onPress) {
		super(x, y, 14, 11, Component.literal(String.valueOf(number)));
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		onPress.run();
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		down = true;
		super.onClick(mouseX, mouseY);
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		down = false;
	}

	@Override
	protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		boolean pressed = down && isHovered();
		PwGuiTextures face = pressed ? PwGuiTextures.TAG_PRESSED
				: isHoveredOrFocused() ? PwGuiTextures.TAG_HOVER : PwGuiTextures.TAG;
		face.render(g, getX(), getY());
		int shift = pressed ? 1 : 0;
		g.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + 7 + shift, getY() + 2 + shift,
				isHoveredOrFocused() ? PwGui.VALUE : PwGui.LABEL);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
