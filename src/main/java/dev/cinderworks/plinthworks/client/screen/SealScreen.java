package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.menu.SealMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SealScreen extends AbstractContainerScreen<SealMenu>
{
	private static final ResourceLocation TEXTURE =
			Plinthworks.id("textures/gui/container/seal_config.png");
	private static final int LABEL = 0x404040;

	private Button whitelistButton;
	private Button blacklistButton;

	public SealScreen(SealMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 166;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		whitelistButton = Button.builder(Component.translatable("gui.plinthworks.mode_whitelist"), b -> send(SealMenu.TOGGLE))
				.bounds(leftPos + 8, topPos + 44, 52, 16).build();
		blacklistButton = Button.builder(Component.translatable("gui.plinthworks.mode_blacklist"), b -> send(SealMenu.TOGGLE))
				.bounds(leftPos + 62, topPos + 44, 52, 16).build();
		addRenderableWidget(whitelistButton);
		addRenderableWidget(blacklistButton);
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.clear_keys"), b -> send(SealMenu.CLEAR))
				.bounds(leftPos + 116, topPos + 44, 52, 16).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// grey out the active mode so the current choice reads as selected
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
		graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL, false);
		graphics.drawString(font, Component.translatable(menu.whitelist()
				? "gui.plinthworks.whitelist" : "gui.plinthworks.blacklist", menu.keyCount()),
				8, 64, LABEL, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL, false);
	}

	private void send(int id) {
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
	}
}
