package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.menu.SealMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SealScreen extends AbstractContainerScreen<SealMenu>
{
	private static final ResourceLocation TEXTURE =
			Plinthworks.id("textures/gui/container/seal_config.png");

	private PwTextButton whitelistButton;
	private PwTextButton blacklistButton;

	public SealScreen(SealMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 166;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		whitelistButton = new PwTextButton(leftPos + 8, topPos + 44, 48, 16,
				Component.translatable("gui.plinthworks.mode_whitelist"), () -> send(SealMenu.TOGGLE));
		blacklistButton = new PwTextButton(leftPos + 58, topPos + 44, 48, 16,
				Component.translatable("gui.plinthworks.mode_blacklist"), () -> send(SealMenu.TOGGLE));
		addRenderableWidget(whitelistButton);
		addRenderableWidget(blacklistButton);
		addRenderableWidget(new PwTextButton(leftPos + 108, topPos + 44, 60, 16,
				Component.translatable("gui.plinthworks.clear_keys"), () -> send(SealMenu.CLEAR)));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// the active mode goes flat so the current choice reads as selected
		whitelistButton.active = !menu.whitelist();
		blacklistButton.active = menu.whitelist();
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, PwGui.TITLE, false);
		graphics.drawString(font, Component.translatable(menu.whitelist()
				? "gui.plinthworks.whitelist" : "gui.plinthworks.blacklist", menu.keyCount()),
				82, titleLabelY, PwGui.VALUE, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, PwGui.MUTED, false);
	}

	private void send(int id) {
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
	}
}
