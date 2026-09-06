package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.logic.network.TransferMode;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class NetworkOverviewScreen extends Screen
{
	private final PlinthScreen parent;
	private final PlinthMenu menu;
	private EditBox channelName;

	public NetworkOverviewScreen(PlinthScreen parent, PlinthMenu menu) {
		super(Component.translatable("gui.plinthworks.network_members"));
		this.parent = parent;
		this.menu = menu;
	}

	@Override
	protected void init() {
		int x = width / 2 - 90;
		int y = height / 2 - 72;
		channelName = new EditBox(font, x, y + 116, 180, 18,
				Component.translatable("gui.plinthworks.network_name"));
		channelName.setMaxLength(32);
		channelName.setValue(menu.blockEntity().channel());
		addRenderableWidget(channelName);
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.network_mode"),
				button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 20))
				.bounds(x, y + 138, 86, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.network_rename"),
				button -> renameChannel()).bounds(x + 94, y + 138, 86, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		int x = width / 2 - 90;
		int y = height / 2 - 72;
		graphics.drawString(font, title, x, y, 0xffffff, false);
		String mode = TransferMode.values()[menu.transferMode()].name().toLowerCase();
		graphics.drawString(font, Component.literal(mode), x, y + 13, 0xbcb4aa, false);
		if (menu.networkMembers().isEmpty()) {
			graphics.drawString(font, Component.translatable("gui.plinthworks.network_empty"),
					x, y + 29, 0xbcb4aa, false);
			return;
		}
		int row = 0;
		for (BlockPos pos : menu.networkMembers()) {
			graphics.drawString(font, Component.literal(pos.toShortString()),
					x, y + 29 + row++ * 11, 0xbcb4aa, false);
			if (row == 7) {
				break;
			}
		}
	}

	private void renameChannel() {
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 30);
		channelName.getValue().chars().forEach(value ->
				minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1000 + value));
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 31);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
