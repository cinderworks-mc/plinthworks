package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// stretched stone button with four looks: normal, hovered, pressed and disabled. a disabled one reads as the selected choice
class PwTextButton extends AbstractButton
{
	private static final ResourceLocation NORMAL = Plinthworks.id("button_normal");
	private static final ResourceLocation HOVER = Plinthworks.id("button_hover");
	private static final ResourceLocation PRESSED = Plinthworks.id("button_pressed");
	private static final ResourceLocation DISABLED = Plinthworks.id("button_disabled");

	private final Runnable onPress;
	private boolean down;

	PwTextButton(int x, int y, int width, int height, Component label, Runnable onPress) {
		super(x, y, width, height, label);
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
		boolean pressed = active && down && isHovered();
		ResourceLocation sprite = !active ? DISABLED : pressed ? PRESSED : isHoveredOrFocused() ? HOVER : NORMAL;
		g.blitSprite(sprite, getX(), getY(), getWidth(), getHeight());
		int color = !active ? PwGui.TITLE : isHoveredOrFocused() ? 0xFFFFFF : PwGui.VALUE;
		int shift = pressed ? 1 : 0;
		g.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + getWidth() / 2 + shift,
				getY() + (getHeight() - 8) / 2 + shift, color);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
