package dev.cinderworks.plinthworks.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

// 16x16 bronze button: icon + tooltip refreshed from suppliers each frame so mode changes show live
class PwIconButton extends AbstractButton
{
	private final Supplier<PwGuiTextures> icon;
	private final Supplier<Component> tip;
	private final Runnable onPress;

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
	protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		setTooltip(Tooltip.create(tip.get()));
		(isHoveredOrFocused() ? PwGuiTextures.BTN_HOVER : PwGuiTextures.BTN).render(g, getX(), getY());
		icon.get().render(g, getX(), getY());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
