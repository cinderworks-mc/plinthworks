package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.client.PwMotion;
import dev.cinderworks.plinthworks.logic.network.TransferMode;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.net.SetChannelPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class NetworkOverviewScreen extends Screen
{
	private static final int ROW = 12;
	private static final int VISIBLE = 6;
	private static final int LIST_X = 12;
	private static final int LIST_Y = 46;
	private static final int LIST_W = 169;
	private static final int NAME_MAX = 32;
	private static final String[] MODES = {"Priority", "Round robin", "Balanced"};
	private static final int[] SEGMENTS = {52, 68, 51};

	private final PlinthScreen parent;
	private final PlinthMenu menu;
	private final PwTextButton[] modeButtons = new PwTextButton[3];
	private EditBox channelName;
	private int left;
	private int top;
	private int scrollTarget;
	private float scrollShown;

	public NetworkOverviewScreen(PlinthScreen parent, PlinthMenu menu) {
		super(Component.translatable("gui.plinthworks.network_members"));
		this.parent = parent;
		this.menu = menu;
	}

	@Override
	protected void init() {
		left = (width - PwFrame.WIDTH) / 2;
		top = (height - PwFrame.HEIGHT) / 2;
		int segment = left + 12;
		for (int i = 0; i < modeButtons.length; i++) {
			int mode = i;
			modeButtons[i] = new PwTextButton(segment, top + 24, SEGMENTS[i], 16, Component.literal(MODES[i]),
					() -> pickMode(mode));
			addRenderableWidget(modeButtons[i]);
			segment += SEGMENTS[i];
		}
		channelName = new EditBox(font, left + 16, top + 128, 80, 10,
				Component.translatable("gui.plinthworks.network_name"));
		channelName.setBordered(false);
		channelName.setTextColor(PwGui.VALUE);
		channelName.setMaxLength(NAME_MAX);
		channelName.setValue(menu.blockEntity().channel());
		addRenderableWidget(channelName);
		addRenderableWidget(new PwTextButton(left + 141, top + 124, 42, 16,
				Component.translatable("gui.plinthworks.network_rename"), this::renameChannel));
	}

	// the server only cycles, so go round until the wanted mode is current
	private void pickMode(int wanted) {
		int steps = Math.floorMod(wanted - menu.transferMode(), MODES.length);
		for (int i = 0; i < steps; i++) {
			minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 20);
		}
	}

	@Override
	public void tick() {
		super.tick();
		int max = maxScroll();
		scrollTarget = Mth.clamp(scrollTarget, 0, max);
		if (PwMotion.reduced()) {
			scrollShown = scrollTarget;
		} else {
			scrollShown += (scrollTarget - scrollShown) * 0.4F;
			if (Math.abs(scrollTarget - scrollShown) < 0.02F) {
				scrollShown = scrollTarget;
			}
		}
	}

	private int maxScroll() {
		return Math.max(0, menu.networkMembers().size() - VISIBLE);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scrollTarget = Mth.clamp(scrollTarget - (int) Math.signum(scrollY) * 3, 0, maxScroll());
		return true;
	}

	// the frame goes on top of the dimmed world, under the widgets
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(graphics, mouseX, mouseY, partialTick);
		PwFrame.render(graphics, left, top);
		PwFrame.well(graphics, left + LIST_X, top + LIST_Y, LIST_W, ROW * VISIBLE);
		PwFrame.well(graphics, left + 12, top + 124, 126, 16);
		if (channelName != null && channelName.isFocused()) {
			graphics.renderOutline(left + 11, top + 123, 128, 18, 0xFFD2A55A);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// a disabled segment reads as the picked one, so with no channel the row is swapped for a hint instead
		boolean named = !menu.blockEntity().channel().isEmpty();
		int selected = menu.transferMode();
		for (int i = 0; i < modeButtons.length; i++) {
			modeButtons[i].visible = named;
			modeButtons[i].active = named && i != selected;
		}
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawString(font, title, left + 13, top + 5, PwGui.TITLE, false);
		if (!named) {
			Component hint = Component.translatable("gui.plinthworks.network_mode_unnamed");
			graphics.drawString(font, hint, left + 12 + (LIST_W + 2 - font.width(hint)) / 2, top + 28, PwGui.MUTED, false);
		}
		List<BlockPos> members = menu.networkMembers();
		String count = Component.translatable(members.size() == 1 ? "gui.plinthworks.network_count_one" : "gui.plinthworks.network_count", members.size()).getString();
		graphics.drawString(font, count, left + 172 - font.width(count), top + 5, PwGui.MUTED, false);
		if (members.isEmpty()) {
			renderEmpty(graphics);
		} else {
			renderRows(graphics, members, mouseX, mouseY);
		}
		String length = channelName.getValue().length() + "/" + NAME_MAX;
		graphics.drawString(font, length, left + 134 - font.width(length), top + 129, PwGui.MUTED, false);
	}

	private void renderEmpty(GuiGraphics graphics) {
		graphics.pose().pushPose();
		graphics.pose().translate(left + LIST_X + LIST_W / 2 - 16, top + LIST_Y + 12, 0);
		graphics.pose().scale(2F, 2F, 1F);
		PwGuiTextures.NETWORK.render(graphics, 0, 0);
		graphics.pose().popPose();
		Component text = Component.translatable("gui.plinthworks.network_empty");
		graphics.drawString(font, text, left + LIST_X + (LIST_W - font.width(text)) / 2, top + LIST_Y + 50,
				PwGui.MUTED, false);
	}

	private void renderRows(GuiGraphics graphics, List<BlockPos> members, int mouseX, int mouseY) {
		int x = left + LIST_X;
		int y = top + LIST_Y;
		int rowsWidth = LIST_W - 6;
		graphics.enableScissor(x + 1, y + 1, x + LIST_W, y + ROW * VISIBLE);
		int offset = Math.round(scrollShown * ROW);
		BlockPos here = menu.blockEntity().getBlockPos();
		for (int i = 0; i < members.size(); i++) {
			int ry = y + i * ROW - offset;
			if (ry + ROW < y || ry > y + ROW * VISIBLE) {
				continue;
			}
			boolean hover = mouseX >= x && mouseX < x + rowsWidth && mouseY >= Math.max(ry, y)
					&& mouseY < Math.min(ry + ROW, y + ROW * VISIBLE);
			BlockPos pos = members.get(i);
			boolean self = pos.equals(here);
			if (hover) {
				graphics.fill(x + 1, ry, x + rowsWidth, ry + ROW, PwGui.PLATE | 0xFF000000);
				graphics.fill(x + 1, ry, x + 3, ry + ROW, 0xFFF08A3C);
			}
			Component name = Component.literal("Plinth " + (i + 1) + (self ? " (here)" : ""));
			String coords = pos.toShortString();
			int nameEnd = x + 6 + font.width(name);
			int coordsStart = x + rowsWidth - 4 - font.width(coords);
			graphics.drawString(font, name, x + 6, ry + 2, self ? PwGui.BRASS : PwGui.LABEL, false);
			graphics.drawString(font, coords, coordsStart, ry + 2, PwGui.VALUE, false);
			for (int dot = nameEnd + 4; dot < coordsStart - 3; dot += 3) {
				graphics.fill(dot, ry + 8, dot + 1, ry + 9, 0xFF4A4744);
			}
		}
		graphics.disableScissor();
		renderScrollbar(graphics, members.size());
	}

	private void renderScrollbar(GuiGraphics graphics, int count) {
		int tx = left + LIST_X + LIST_W - 5;
		int ty = top + LIST_Y + 1;
		int th = ROW * VISIBLE - 1;
		graphics.fill(tx, ty, tx + 4, ty + th, 0xFF17161A);
		graphics.fill(tx + 4, ty, tx + 5, ty + th, 0xFF4A4744);
		if (count <= VISIBLE) {
			return;
		}
		int thumb = Math.max(8, th * VISIBLE / count);
		int travel = th - thumb;
		int at = ty + Math.round(travel * (scrollShown / maxScroll()));
		graphics.fill(tx, at, tx + 4, at + thumb, 0xFFB38A4A);
		graphics.fill(tx, at, tx + 4, at + 1, 0xFFD2A55A);
	}

	private void renameChannel() {
		PacketDistributor.sendToServer(new SetChannelPayload(channelName.getValue()));
	}

	// in singleplayer a pausing screen holds back the sync, so a rename or mode pick would not show until it closed
	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
