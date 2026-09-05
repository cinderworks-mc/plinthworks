package dev.hartforge.plinthworks.client.screen;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.item.SigilType;
import dev.hartforge.plinthworks.menu.PlinthMenu;
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

	// panel texture is dark, so labels use parchment tones instead of the vanilla near-black
	private static final int HEADING = 0xE8D6AE;
	private static final int LABEL = 0xCBBB97;

	// right stat column and the two side buttons; both live left of imageWidth (176)
	private static final int COL2 = 98;
	private static final int BUTTON_X = 130;
	private static final int BUTTON_W = 42;

	public PlinthScreen(PlinthMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageHeight = 219;
		inventoryLabelY = 127;
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.configure_seal"),
				button -> minecraft.setScreen(new SealConfigScreen(this, menu)))
				.bounds(leftPos + BUTTON_X, topPos + 49, BUTTON_W, 12).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.network_overview"),
				button -> minecraft.setScreen(new NetworkOverviewScreen(this, menu)))
				.bounds(leftPos + BUTTON_X, topPos + 63, BUTTON_W, 12).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		WorkAreaPreview.drawStub(graphics, leftPos + 130, topPos + 29);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, HEADING, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.sigil"), 8, 17, HEADING, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.etchings"), 50, 17, HEADING, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.display"), 8, 47, HEADING, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.seals"), 69, 47, HEADING, false);
		graphics.drawString(font, verbLine(), 8, 79, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.interval", menu.intervalTicks()),
				8, 91, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.throughput", menu.throughput()),
				COL2, 91, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.storage", menu.bufferSlots()),
				8, 103, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.range", menu.range()),
				COL2, 103, LABEL, false);
		String mode = TransferMode.values()[menu.transferMode()].name().toLowerCase();
		String channel = menu.blockEntity().channel().isEmpty() ? "local" : menu.blockEntity().channel();
		graphics.drawString(font, Component.translatable("gui.plinthworks.network", channel,
				menu.channelMembers(), mode), 8, 115, LABEL, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL, false);
	}

	private Component verbLine() {
		int value = menu.verb();
		String name = value == 0 ? "idle" : SigilType.values()[value - 1].name().toLowerCase();
		return Component.translatable("gui.plinthworks.verb", name);
	}
}
