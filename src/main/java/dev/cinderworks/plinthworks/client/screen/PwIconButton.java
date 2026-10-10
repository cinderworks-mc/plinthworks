package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.client.PwMotion;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

// 16x16 stone button: icon and tooltip text come from suppliers so mode changes show live
class PwIconButton extends AbstractButton
{
	private static final long FLASH_MS = 200;

	private final Supplier<PwGuiTextures> icon;
	private final Supplier<Component> tip;
	private final BooleanSupplier toggled;
	private final Runnable onPress;
	private Component shownTip;
	private PwGuiTextures lastIcon;
	private long flashUntil;
	private boolean down;

	PwIconButton(int x, int y, Supplier<PwGuiTextures> icon, Supplier<Component> tip, Runnable onPress) {
		this(x, y, icon, tip, () -> false, onPress);
	}

	PwIconButton(int x, int y, Supplier<PwGuiTextures> icon, Supplier<Component> tip, BooleanSupplier toggled,
			Runnable onPress) {
		super(x, y, 16, 16, Component.empty());
		this.icon = icon;
		this.tip = tip;
		this.toggled = toggled;
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
		boolean on = toggled.getAsBoolean();
		PwGuiTextures current = icon.get();
		// a new mode dips the button for a moment so the change is noticed
		if (lastIcon != null && current != lastIcon && !PwMotion.reduced()) {
			flashUntil = Util.getMillis() + FLASH_MS;
		}
		lastIcon = current;
		boolean pressed = active && ((down && isHovered()) || Util.getMillis() < flashUntil);
		PwGuiTextures face = !active ? PwGuiTextures.BTN_DISABLED
				: pressed ? PwGuiTextures.BTN_PRESSED
				: on ? PwGuiTextures.BTN_TOGGLED
				: isHovered() ? PwGuiTextures.BTN_HOVER : PwGuiTextures.BTN;
		if (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) {
			PwGuiTextures.BTN_FOCUS.render(g, getX() - 2, getY() - 2);
		}
		face.render(g, getX(), getY());
		int shift = pressed || on ? 1 : 0;
		if (!active) {
			g.setColor(0.55F, 0.55F, 0.55F, 1F);
		} else if (on) {
			// the engraved stroke turns brass when the mode is doing something
			g.setColor(1F, 0.82F, 0.49F, 1F);
		}
		current.render(g, getX() + shift, getY() + shift);
		g.setColor(1F, 1F, 1F, 1F);
	}

	// the tooltip is rebuilt only when its text changes, not every frame
	private void refreshTooltip() {
		Component now = tip.get();
		if (!now.equals(shownTip)) {
			shownTip = now;
			setTooltip(Tooltip.create(now));
			setMessage(now);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
