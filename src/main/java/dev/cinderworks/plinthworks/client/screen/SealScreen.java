package dev.cinderworks.plinthworks.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.menu.SealMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SealScreen extends AbstractContainerScreen<SealMenu>
{
	private static final ResourceLocation TEXTURE =
			Plinthworks.id("textures/gui/container/seal_config.png");
	private static final int CONFIRM_TICKS = 60;

	private PwTextButton whitelistButton;
	private PwTextButton blacklistButton;
	private PwTextButton clearButton;
	private int armed;

	public SealScreen(SealMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 178;
		imageHeight = 170;
		titleLabelX = 13;
		titleLabelY = 5;
		inventoryLabelY = 77;
	}

	@Override
	protected void init() {
		super.init();
		whitelistButton = new PwTextButton(leftPos + 72, topPos + 22, 46, 16,
				Component.translatable("gui.plinthworks.mode_whitelist"), () -> send(SealMenu.TOGGLE));
		blacklistButton = new PwTextButton(leftPos + 118, topPos + 22, 46, 16,
				Component.translatable("gui.plinthworks.mode_blacklist"), () -> send(SealMenu.TOGGLE));
		clearButton = new PwTextButton(leftPos + 72, topPos + 44, 92, 14,
				Component.translatable("gui.plinthworks.clear_keys"), this::clearKeys).danger();
		addRenderableWidget(whitelistButton);
		addRenderableWidget(blacklistButton);
		addRenderableWidget(clearButton);
	}

	// the first click arms the button, a second one within three seconds clears
	private void clearKeys() {
		if (armed > 0) {
			armed = 0;
			clearButton.setMessage(Component.translatable("gui.plinthworks.clear_keys"));
			send(SealMenu.CLEAR);
		} else {
			armed = CONFIRM_TICKS;
			clearButton.setMessage(Component.literal("Sure?"));
		}
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		if (armed > 0 && --armed == 0) {
			clearButton.setMessage(Component.translatable("gui.plinthworks.clear_keys"));
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// the active mode goes flat so the current choice reads as selected
		whitelistButton.active = !menu.whitelist();
		blacklistButton.active = menu.whitelist();
		super.render(graphics, mouseX, mouseY, partialTick);
		if (hoveredSlot != null && hoveredSlot.isActive()) {
			graphics.renderOutline(leftPos + hoveredSlot.x - 1, topPos + hoveredSlot.y - 1, 18, 18, 0xFFD2A55A);
		}
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	public int getSlotColor(int index) {
		return 0x30B38A4A;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.enableBlend();
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
		for (int i = 0; i < SealMenu.GHOST; i++) {
			Slot slot = menu.slots.get(i);
			int x = leftPos + slot.x;
			int y = topPos + slot.y;
			if (slot.hasItem()) {
				// a filled key keeps a brass underline
				graphics.fill(x + 1, y + 17, x + 15, y + 18, 0xFFD2A55A);
			} else {
				PwGuiTextures.PLUS.render(graphics, x + 5, y + 5);
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, PwGui.TITLE, false);
		graphics.drawString(font, Component.translatable(menu.whitelist()
				? "gui.plinthworks.whitelist" : "gui.plinthworks.blacklist", menu.keyCount()),
				72, 62, PwGui.VALUE, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, PwGui.MUTED, false);
	}

	private void send(int id) {
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
	}
}
