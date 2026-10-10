package dev.cinderworks.plinthworks.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

// 16x16 stone button: icon and tooltip text come from suppliers so mode changes show live
class PwIconButton extends AbstractButton
{
	private final Supplier<PwGuiTextures> icon;
	private final Supplier<Component> tip;
	private final Runnable onPress;
	private Component shownTip;
	private boolean down;

	PwIconButton(int x, int y, Supplier<PwGuiTextures> icon, Supplier<Component> tip, Runnable onPress) {
		super(x, y, 16, 16, Component.empty());
		this.icon = icon;
		this.tip = tip;
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
		refreshTooltip();
		boolean pressed = active && down && isHovered();
		PwGuiTextures face = !active ? PwGuiTextures.BTN_DISABLED
				: pressed ? PwGuiTextures.BTN_PRESSED
				: isHoveredOrFocused() ? PwGuiTextures.BTN_HOVER : PwGuiTextures.BTN;
		face.render(g, getX(), getY());
		int shift = pressed ? 1 : 0;
		if (!active) {
			g.setColor(0.55F, 0.55F, 0.55F, 1F);
		}
		icon.get().render(g, getX() + shift, getY() + shift);
		g.setColor(1F, 1F, 1F, 1F);
	}

	// the tooltip is rebuilt only when its text changes, not every frame
	private void refreshTooltip() {
		Component now = tip.get();
		if (!now.equals(shownTip)) {
			shownTip = now;
			setTooltip(Tooltip.create(now));
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
