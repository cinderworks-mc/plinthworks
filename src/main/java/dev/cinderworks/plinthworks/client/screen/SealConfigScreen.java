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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class SealConfigScreen extends Screen
{
	// four key rows fit between the top buttons and the bottom of the frame
	static final int VISIBLE = 4;
	private static final int ROW = 20;
	private static final int LIST_Y = 46;
	private static final int KEY_W = 172;

	private final PlinthScreen parent;
	private final PlinthMenu menu;
	private final int sealIndex;
	private final List<PwTextButton> keyButtons = new ArrayList<>();
	private PwTextButton clearButton;
	private int scroll;
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

		keyButtons.clear();
		ItemStack shown = menu.blockEntity().getDisplayedItem();
		if (shown.isEmpty()) {
			return;
		}
		// the server takes a key by its index in this same order, and nothing past its limit
		if (seal.type() == SealType.TAG) {
			addKeyButtons(SealMatch.sortedTags(shown).stream().limit(PlinthMenu.KEY_INDEX_LIMIT)
					.map(Object::toString).toList(), SealEdit.ADD_TAG);
		} else if (seal.type() == SealType.COMPONENT) {
			addKeyButtons(shown.getComponents().stream().limit(PlinthMenu.KEY_INDEX_LIMIT)
					.map(SealConfigScreen::componentName).toList(), SealEdit.ADD_COMPONENT);
		} else {
			addKeyButtons(List.of(Component.translatable("gui.plinthworks.add_displayed").getString()),
					SealEdit.ADD_DISPLAYED);
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

	// the button index is the key index the server gets. a long list loses a few pixels to the scrollbar
	private void addKeyButtons(List<String> labels, SealEdit action) {
		int width = labels.size() > VISIBLE ? KEY_W - 7 : KEY_W;
		for (int i = 0; i < labels.size(); i++) {
			int key = i;
			keyButtons.add(addRenderableWidget(new PwTextButton(left + 12, top + LIST_Y, width, 18,
					Component.literal(labels.get(i)), () -> send(action, key))));
		}
		layoutKeys();
	}

	// only the rows inside the window are shown, the rest wait hidden until a scroll brings them in
	private void layoutKeys() {
		scroll = clampScroll(scroll, keyButtons.size());
		for (int i = 0; i < keyButtons.size(); i++) {
			int row = i - scroll;
			keyButtons.get(i).visible = row >= 0 && row < VISIBLE;
			keyButtons.get(i).setY(top + LIST_Y + row * ROW);
		}
	}

	static int clampScroll(int scroll, int count) {
		return Mth.clamp(scroll, 0, Math.max(0, count - VISIBLE));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (keyButtons.size() <= VISIBLE) {
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}
		scroll -= (int) Math.signum(scrollY);
		layoutKeys();
		return true;
	}

	// same track and thumb as the member list on the network screen
	private void renderScrollbar(GuiGraphics graphics) {
		int count = keyButtons.size();
		if (count <= VISIBLE) {
			return;
		}
		int tx = left + 12 + KEY_W - 5;
		int ty = top + LIST_Y;
		int th = ROW * VISIBLE - 2;
		graphics.fill(tx, ty, tx + 4, ty + th, 0xFF17161A);
		graphics.fill(tx + 4, ty, tx + 5, ty + th, 0xFF4A4744);
		int thumb = Math.max(8, th * VISIBLE / count);
		int at = ty + Math.round((th - thumb) * (scroll / (float) (count - VISIBLE)));
		graphics.fill(tx, at, tx + 4, at + thumb, 0xFFB38A4A);
		graphics.fill(tx, at, tx + 4, at + 1, 0xFFD2A55A);
	}

	private static String componentName(TypedDataComponent<?> component) {
		return component.type().toString();
	}

	private void send(SealEdit action, int key) {
		PacketDistributor.sendToServer(new SealEditPayload(action, sealIndex, key));
	}

	// in singleplayer a pausing screen holds back the sync, so a picked key would not show until it closed
	@Override
	public boolean isPauseScreen() {
		return false;
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
		renderScrollbar(graphics);
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
