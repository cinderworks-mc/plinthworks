package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.logic.network.TransferMode;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.net.SetChannelPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

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
		channelName = new EditBox(font, x + 5, y + 121, 170, 10,
				Component.translatable("gui.plinthworks.network_name"));
		channelName.setBordered(false);
		channelName.setTextColor(PwGui.VALUE);
		channelName.setMaxLength(32);
		channelName.setValue(menu.blockEntity().channel());
		addRenderableWidget(channelName);
		addRenderableWidget(new PwTextButton(x, y + 138, 86, 20, Component.translatable("gui.plinthworks.network_mode"),
				() -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 20)));
		addRenderableWidget(new PwTextButton(x + 94, y + 138, 86, 20,
				Component.translatable("gui.plinthworks.network_rename"), this::renameChannel));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		int x = width / 2 - 90;
		int y = height / 2 - 72;
		PwGui.panel(graphics, x - 12, y - 12, 204, 182);
		PwGui.inset(graphics, x, y + 26, 180, 84);
		PwGui.inset(graphics, x, y + 116, 180, 18);
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawString(font, title, x, y, PwGui.TITLE, false);
		Component mode = Component.translatable("gui.plinthworks.transfer."
				+ TransferMode.values()[menu.transferMode()].name().toLowerCase(Locale.ROOT));
		graphics.drawString(font, mode, x, y + 13, PwGui.MUTED, false);
		if (menu.networkMembers().isEmpty()) {
			graphics.drawString(font, Component.translatable("gui.plinthworks.network_empty"),
					x + 5, y + 31, PwGui.MUTED, false);
			return;
		}
		int row = 0;
		for (BlockPos pos : menu.networkMembers()) {
			graphics.drawString(font, Component.literal(pos.toShortString()),
					x + 5, y + 31 + row++ * 11, PwGui.TEXT, false);
			if (row == 7) {
				break;
			}
		}
	}

	private void renameChannel() {
		PacketDistributor.sendToServer(new SetChannelPayload(channelName.getValue()));
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
