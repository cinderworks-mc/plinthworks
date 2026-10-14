package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.component.SealConfig;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.seal.SealMatch;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.net.SealEdit;
import dev.cinderworks.plinthworks.net.SealEditPayload;
import dev.cinderworks.plinthworks.registry.ModDataComponents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class SealConfigScreen extends Screen
{
	private final PlinthScreen parent;
	private final PlinthMenu menu;
	private final int sealIndex;
	private PwTextButton clearButton;
	private int armed;
	private int left;
	private int top;

	public SealConfigScreen(PlinthScreen parent, PlinthMenu menu, int sealIndex) {
		super(Component.translatable("gui.plinthworks.seal_config"));
		this.parent = parent;
		this.menu = menu;
		this.sealIndex = sealIndex;
	}

	@Override
	protected void init() {
		left = (width - PwFrame.WIDTH) / 2;
		top = (height - PwFrame.HEIGHT) / 2;
		ItemStack sealStack = menu.seal(sealIndex);
		if (!(sealStack.getItem() instanceof SealItem seal)) {
			return;
		}
		addRenderableWidget(new PwTextButton(left + 12, top + 24, 86, 16,
				Component.translatable("gui.plinthworks.toggle_mode"), () -> send(SealEdit.TOGGLE_MODE, 0)));
		clearButton = new PwTextButton(left + 98, top + 24, 86, 16,
				Component.translatable("gui.plinthworks.clear_keys"), this::clearKeys).danger();
		addRenderableWidget(clearButton);

		ItemStack shown = menu.blockEntity().getDisplayedItem();
		if (shown.isEmpty()) {
			return;
		}
		if (seal.type() == SealType.TAG) {
			SealMatch.sortedTags(shown).stream().limit(4).forEach(tag ->
					addKeyButton(tag.toString(), SealEdit.ADD_TAG, nextKeyButton()));
		} else if (seal.type() == SealType.COMPONENT) {
			shown.getComponents().stream().limit(4).forEach(component ->
					addKeyButton(componentName(component), SealEdit.ADD_COMPONENT, nextKeyButton()));
		} else {
			addKeyButton(Component.translatable("gui.plinthworks.add_displayed").getString(),
					SealEdit.ADD_DISPLAYED, 0);
		}
	}

	// the first click arms the button, a second one within three seconds clears
	private void clearKeys() {
		if (armed > 0) {
			armed = 0;
			clearButton.setMessage(Component.translatable("gui.plinthworks.clear_keys"));
			send(SealEdit.CLEAR_KEYS, 0);
		} else {
			armed = 60;
			clearButton.setMessage(Component.literal("Sure?"));
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (armed > 0 && --armed == 0) {
			clearButton.setMessage(Component.translatable("gui.plinthworks.clear_keys"));
		}
	}

	private int nextKeyButton() {
		return Math.max(0, children().size() - 2);
	}

	private void addKeyButton(String label, SealEdit action, int key) {
		int row = nextKeyButton();
		addRenderableWidget(new PwTextButton(left + 12, top + 46 + row * 20, 172, 18,
				Component.literal(label), () -> send(action, key)));
	}

	private static String componentName(TypedDataComponent<?> component) {
		return component.type().toString();
	}

	private void send(SealEdit action, int key) {
		PacketDistributor.sendToServer(new SealEditPayload(action, sealIndex, key));
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	// the frame goes on top of the dimmed world, under the widgets
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(graphics, mouseX, mouseY, partialTick);
		PwFrame.render(graphics, left, top);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawString(font, title, left + 13, top + 5, PwGui.TITLE, false);
		ItemStack seal = menu.seal(sealIndex);
		if (!(seal.getItem() instanceof SealItem)) {
			graphics.drawString(font, Component.translatable("gui.plinthworks.no_seal"), left + 12, top + 28,
					PwGui.MUTED, false);
			return;
		}
		SealConfig config = seal.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY);
		Component mode = Component.translatable(config.whitelist()
				? "gui.plinthworks.whitelist" : "gui.plinthworks.blacklist", config.keys().size());
		graphics.drawString(font, mode, left + 172 - font.width(mode), top + 5, PwGui.MUTED, false);
	}
}
