package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.component.SealConfig;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.registry.ModDataComponents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SealConfigScreen extends Screen
{
	private final PlinthScreen parent;
	private final PlinthMenu menu;
	private final int sealIndex;

	public SealConfigScreen(PlinthScreen parent, PlinthMenu menu, int sealIndex) {
		super(Component.translatable("gui.plinthworks.seal_config"));
		this.parent = parent;
		this.menu = menu;
		this.sealIndex = sealIndex;
	}

	@Override
	protected void init() {
		// select this seal on the menu so every op below writes to it, not slot 0
		send(40 + sealIndex);
		int x = width / 2 - 90;
		int y = height / 2 - 65;
		ItemStack sealStack = menu.seal(sealIndex);
		if (!(sealStack.getItem() instanceof SealItem seal)) {
			return;
		}
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.toggle_mode"), b -> send(0))
				.bounds(x, y + 24, 86, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.plinthworks.clear_keys"), b -> send(1))
				.bounds(x + 94, y + 24, 86, 20).build());

		ItemStack shown = menu.blockEntity().getDisplayedItem();
		if (shown.isEmpty()) {
			return;
		}
		if (seal.type() == SealType.TAG) {
			shown.getTags().limit(4).forEach(tag -> addKeyButton(tag.location().toString(), 100 + nextKeyButton(), x, y));
		} else if (seal.type() == SealType.COMPONENT) {
			shown.getComponents().stream().limit(4).forEach(component ->
					addKeyButton(componentName(component), 200 + nextKeyButton(), x, y));
		} else {
			addKeyButton(Component.translatable("gui.plinthworks.add_displayed").getString(), 2, x, y);
		}
	}

	private int nextKeyButton() {
		return Math.max(0, children().size() - 2);
	}

	private void addKeyButton(String label, int id, int x, int y) {
		int row = nextKeyButton();
		addRenderableWidget(Button.builder(Component.literal(label), b -> send(id))
				.bounds(x, y + 50 + row * 22, 180, 20).build());
	}

	private static String componentName(TypedDataComponent<?> component) {
		return component.type().toString();
	}

	private void send(int id) {
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		int x = width / 2 - 90;
		int y = height / 2 - 65;
		graphics.drawString(font, title, x, y, 0xffffff, false);
		ItemStack seal = menu.seal(sealIndex);
		if (!(seal.getItem() instanceof SealItem)) {
			graphics.drawString(font, Component.translatable("gui.plinthworks.no_seal"), x, y + 12, 0xbcb4aa, false);
			return;
		}
		SealConfig config = seal.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY);
		graphics.drawString(font, Component.translatable(config.whitelist()
				? "gui.plinthworks.whitelist" : "gui.plinthworks.blacklist", config.keys().size()),
				x, y + 12, 0xbcb4aa, false);
	}
}
