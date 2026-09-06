package dev.hartforge.plinthworks.client.screen;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.item.SigilType;
import dev.hartforge.plinthworks.menu.PlinthMenu;
import dev.hartforge.plinthworks.logic.RedstoneMode;
import dev.hartforge.plinthworks.logic.network.TransferMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PlinthScreen extends AbstractContainerScreen<PlinthMenu>
{
	private static final ResourceLocation TEXTURE =
			Plinthworks.id("textures/gui/container/plinth.png");

	// vanilla-style gray panel, so labels use the near-black vanilla ink
	private static final int LABEL = 0x404040;

	// recessed stats panel, texture rect x146..206 y13..76
	private static final int STAT_X = 149;
	private static final int STAT_TOP = 15;

	private Button modeButton;
	private Button redstoneButton;

	public PlinthScreen(PlinthMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 214;
		imageHeight = 208;
		inventoryLabelY = 96;
	}

	@Override
	protected void init() {
		super.init();
		for (int i = 0; i < menu.blockEntity().seals().getSlots(); i++) {
			int index = i;
			addRenderableWidget(Button.builder(Component.literal(String.valueOf(i + 1)),
					b -> minecraft.setScreen(new SealConfigScreen(this, menu, index)))
					.bounds(leftPos + 53 + i * 18, topPos + 74, 16, 10).build());
		}
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.network_overview"),
				b -> minecraft.setScreen(new NetworkOverviewScreen(this, menu)))
				.bounds(leftPos + 146, topPos + 75, 60, 10).build());
		modeButton = Button.builder(modeName(), b -> send(20))
				.bounds(leftPos + 146, topPos + 86, 60, 10).build();
		addRenderableWidget(modeButton);
		redstoneButton = Button.builder(redstoneName(), b -> send(60))
				.bounds(leftPos + 146, topPos + 97, 60, 10).build();
		addRenderableWidget(redstoneButton);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		modeButton.setMessage(modeName());
		redstoneButton.setMessage(redstoneName());
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
		graphics.drawString(font, Component.translatable("gui.plinthworks.sigil"), 10, 15, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.etchings"), 54, 15, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.display"), 10, 45, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.seals"), 54, 45, LABEL, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL, false);
		renderStats(graphics);
	}

	private void renderStats(GuiGraphics graphics) {
		int value = menu.verb();
		String verb = value == 0 ? "idle" : SigilType.values()[value - 1].name().toLowerCase();
		graphics.drawString(font, Component.literal(title(verb)), STAT_X, STAT_TOP, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.stat_interval", menu.intervalTicks()),
				STAT_X, STAT_TOP + 10, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.stat_throughput", menu.throughput()),
				STAT_X, STAT_TOP + 20, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.stat_storage", menu.bufferSlots()),
				STAT_X, STAT_TOP + 30, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.stat_range", menu.range()),
				STAT_X, STAT_TOP + 40, LABEL, false);
		String channel = menu.blockEntity().channel().isEmpty() ? "local" : menu.blockEntity().channel();
		graphics.drawString(font, Component.translatable("gui.plinthworks.stat_channel",
				trim(channel, 30), menu.channelMembers()), STAT_X, STAT_TOP + 50, LABEL, false);
	}

	private String trim(String text, int maxWidth) {
		while (text.length() > 1 && font.width(text) > maxWidth) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	private Component modeName() {
		return Component.literal(TransferMode.values()[menu.transferMode()].name().toLowerCase());
	}

	private Component redstoneName() {
		return switch (menu.blockEntity().redstoneMode()) {
			case ALWAYS -> Component.translatable("gui.plinthworks.redstone.always");
			case PAUSE_WHEN_POWERED -> Component.translatable("gui.plinthworks.redstone.pause");
			case RUN_WHEN_POWERED -> Component.translatable("gui.plinthworks.redstone.run");
		};
	}

	private void send(int id) {
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
	}

	private static String title(String value) {
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
