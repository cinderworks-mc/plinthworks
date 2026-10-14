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
	private static final int DANGER_EDGE = 0xFFFF7A5A;

	private final Runnable onPress;
	private boolean down;
	private boolean danger;

	PwTextButton(int x, int y, int width, int height, Component label, Runnable onPress) {
		super(x, y, width, height, label);
		this.onPress = onPress;
	}

	// a destructive button gets a coloured bottom edge
	PwTextButton danger() {
		danger = true;
		return this;
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
		if (danger && active) {
			g.fill(getX() + 2, getY() + getHeight() - 2, getX() + getWidth() - 2, getY() + getHeight() - 1, DANGER_EDGE);
		}
		int color = !active ? PwGui.BRASS : isHoveredOrFocused() ? PwGui.VALUE : PwGui.LABEL;
		int shift = pressed ? 1 : 0;
		// long tag and component names must stay inside the button
		var font = Minecraft.getInstance().font;
		Component label = font.width(getMessage()) <= getWidth() - 8 ? getMessage()
				: Component.literal(PwStatPlate.clip(font, getMessage().getString(), getWidth() - 8));
		g.drawCenteredString(font, label, getX() + getWidth() / 2 + shift,
				getY() + (getHeight() - 8) / 2 + shift - (active ? 0 : 1), color);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
