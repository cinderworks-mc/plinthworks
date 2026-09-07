package dev.cinderworks.plinthworks.client.screen;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.SigilType;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import dev.cinderworks.plinthworks.logic.network.TransferMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class PlinthScreen extends AbstractContainerScreen<PlinthMenu>
{
	private static final ResourceLocation TEXTURE =
			Plinthworks.id("textures/gui/container/plinth.png");

	// vanilla-style gray panel, so labels use the near-black vanilla ink
	private static final int LABEL = 0x404040;

	// stat readout column (relative to panel), sits under the control toolbar
	private static final int STAT_ICON_X = 139;
	private static final int STAT_TEXT_X = 150;
	private static final int STAT_TOP = 40;
	private static final int ROW = 12;

	// vertical gauge, drawn where the display slot sits when not in ITEM mode
	private static final int GAUGE_X = 9;
	private static final int GAUGE_Y = 62;
	private static final int GAUGE_INNER = 2;
	private static final int GAUGE_FILL_W = 14;
	private static final int GAUGE_FILL_H = 48;

	private static final int ENERGY_FILL = 0xFFE6A83C;
	private static final int XP_FILL = 0xFF6ABE4A;
	private static final int FLUID_FALLBACK = 0xFF3A6EA5;

	public PlinthScreen(PlinthMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 214;
		imageHeight = 214;
		inventoryLabelY = 123;
	}

	@Override
	protected void init() {
		super.init();
		for (int i = 0; i < menu.blockEntity().seals().getSlots(); i++) {
			int index = i;
			addRenderableWidget(Button.builder(Component.literal(String.valueOf(i + 1)),
					b -> openSeal(index))
					.bounds(leftPos + 57 + i * 18, topPos + 84, 16, 11).build());
		}
		int tx = leftPos + 138;
		int ty = topPos + 18;
		addRenderableWidget(new PwIconButton(tx, ty, this::resourceIcon,
				() -> Component.translatable("gui.plinthworks.resource.tip.state", resourceName()), () -> send(80)));
		addRenderableWidget(new PwIconButton(tx + 18, ty, this::transferIcon,
				() -> Component.translatable("gui.plinthworks.mode.tip.state", transferName()), () -> send(20)));
		addRenderableWidget(new PwIconButton(tx + 36, ty, () -> PwGuiTextures.REDSTONE,
				this::redstoneTip, () -> send(60)));
		addRenderableWidget(new PwIconButton(tx + 54, ty, () -> PwGuiTextures.NETWORK,
				() -> Component.translatable("gui.plinthworks.network_overview"),
				() -> minecraft.setScreen(new NetworkOverviewScreen(this, menu))));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderGaugeTooltip(graphics, mouseX, mouseY);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
		if (menu.resourceMode() != ResourceMode.ITEM) {
			drawGauge(graphics);
		}
	}

	private void drawGauge(GuiGraphics graphics) {
		int fx = leftPos + GAUGE_X;
		int fy = topPos + GAUGE_Y;
		PwGuiTextures.GAUGE_FRAME.render(graphics, fx, fy);
		int innerX = fx + GAUGE_INNER;
		int innerBottom = fy + GAUGE_INNER + GAUGE_FILL_H;
		float frac = gaugeFraction();
		int fillPx = Math.round(frac * GAUGE_FILL_H);
		if (fillPx > 0) {
			int top = innerBottom - fillPx;
			if (menu.resourceMode() == ResourceMode.FLUID) {
				fillFluid(graphics, innerX, top, innerBottom);
			} else {
				int color = menu.resourceMode() == ResourceMode.ENERGY ? ENERGY_FILL : XP_FILL;
				graphics.fill(innerX, top, innerX + GAUGE_FILL_W, innerBottom, color);
			}
		}
		drawGaugeLabel(graphics, fx);
	}

	// tile the fluid's own still sprite up the fill, clipped to the filled height
	private void fillFluid(GuiGraphics graphics, int innerX, int top, int innerBottom) {
		FluidStack fluid = menu.blockEntity().fluid().getFluid();
		if (fluid.isEmpty()) {
			graphics.fill(innerX, top, innerX + GAUGE_FILL_W, innerBottom, FLUID_FALLBACK);
			return;
		}
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
		TextureAtlasSprite sprite = Minecraft.getInstance()
				.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fluid));
		int tint = ext.getTintColor(fluid);
		graphics.setColor(((tint >> 16) & 0xFF) / 255f, ((tint >> 8) & 0xFF) / 255f, (tint & 0xFF) / 255f, 1f);
		graphics.enableScissor(innerX, top, innerX + GAUGE_FILL_W, innerBottom);
		for (int y = innerBottom - 16; y + 16 > top; y -= 16) {
			graphics.blit(innerX, y, 0, GAUGE_FILL_W, 16, sprite);
		}
		graphics.disableScissor();
		graphics.setColor(1f, 1f, 1f, 1f);
	}

	private void drawGaugeLabel(GuiGraphics graphics, int fx) {
		int lx = fx + 21;
		graphics.drawString(font, Component.literal(compact(gaugeStored())), lx, topPos + GAUGE_Y + 4, LABEL, false);
		graphics.drawString(font, Component.literal(gaugeUnit()), lx, topPos + GAUGE_Y + 14, LABEL, false);
	}

	// guarded so an unsynced (0) capacity on the first frame never divides by zero
	private float gaugeFraction() {
		return switch (menu.resourceMode()) {
			case ENERGY -> frac(menu.energyStored(), menu.energyCapacity());
			case FLUID, XP -> frac(menu.fluidStored(), menu.fluidCapacity());
			default -> 0f;
		};
	}

	private int gaugeStored() {
		return switch (menu.resourceMode()) {
			case ENERGY -> menu.energyStored();
			case XP -> menu.fluidStored() / Math.max(1, menu.xpMbPerPoint());
			default -> menu.fluidStored();
		};
	}

	private String gaugeUnit() {
		return switch (menu.resourceMode()) {
			case ENERGY -> "FE";
			case XP -> "XP";
			default -> "mB";
		};
	}

	private static float frac(int stored, int capacity) {
		if (capacity <= 0) {
			return 0f;
		}
		return Math.min(1f, Math.max(0f, stored / (float) capacity));
	}

	private void renderGaugeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		if (menu.resourceMode() == ResourceMode.ITEM) {
			return;
		}
		int fx = leftPos + GAUGE_X;
		int fy = topPos + GAUGE_Y;
		if (mouseX < fx || mouseX >= fx + 18 || mouseY < fy || mouseY >= fy + 52) {
			return;
		}
		graphics.renderComponentTooltip(font, gaugeTooltip(), mouseX, mouseY);
	}

	private List<Component> gaugeTooltip() {
		return switch (menu.resourceMode()) {
			case ENERGY -> List.of(Component.translatable("gui.plinthworks.stat_energy",
					menu.energyStored(), menu.energyCapacity()));
			case XP -> List.of(Component.translatable("gui.plinthworks.stat_xp",
					menu.fluidStored() / Math.max(1, menu.xpMbPerPoint()), menu.fluidStored()));
			default -> List.of(fluidName(), Component.translatable("gui.plinthworks.stat_fluid",
					menu.fluidStored(), menu.fluidCapacity()));
		};
	}

	private Component fluidName() {
		FluidStack fluid = menu.blockEntity().fluid().getFluid();
		return fluid.isEmpty()
				? Component.translatable("gui.plinthworks.fluid_empty")
				: fluid.getHoverName();
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.sigil"), 10, 18, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.etchings"), 57, 18, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.display"), 10, 54, LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.seals"), 57, 54, LABEL, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL, false);
		renderStats(graphics);
	}

	private void renderStats(GuiGraphics graphics) {
		int y = STAT_TOP;
		int value = menu.verb();
		String verb = value == 0 ? "idle" : SigilType.values()[value - 1].name().toLowerCase();
		graphics.drawString(font, Component.literal(title(verb)), STAT_ICON_X, y, LABEL, false);
		y += ROW;
		statRow(graphics, PwGuiTextures.STAT_RATE,
				Component.translatable("gui.plinthworks.val_ticks", menu.intervalTicks()), y);
		y += ROW;
		statRow(graphics, PwGuiTextures.STAT_MOVE, moveValue(), y);
		y += ROW;
		if (menu.resourceMode() == ResourceMode.ITEM) {
			statRow(graphics, PwGuiTextures.STAT_HOLD, Component.literal(String.valueOf(menu.bufferSlots())), y);
			y += ROW;
		}
		statRow(graphics, PwGuiTextures.STAT_REACH, Component.literal(String.valueOf(menu.range())), y);
		y += ROW;
		String channel = menu.blockEntity().channel().isEmpty() ? "local" : menu.blockEntity().channel();
		statRow(graphics, PwGuiTextures.STAT_CHAN,
				Component.translatable("gui.plinthworks.stat_channel", trim(channel, 46), menu.channelMembers()), y);
	}

	private Component moveValue() {
		return switch (menu.resourceMode()) {
			case ITEM -> Component.literal(String.valueOf(menu.throughput()));
			case ENERGY -> Component.translatable("gui.plinthworks.val_fe", menu.energyThroughput());
			case FLUID, XP -> Component.translatable("gui.plinthworks.val_mb", menu.fluidThroughput());
		};
	}

	private void statRow(GuiGraphics graphics, PwGuiTextures icon, Component value, int y) {
		icon.render(graphics, STAT_ICON_X, y);
		graphics.drawString(font, value, STAT_TEXT_X, y, LABEL, false);
	}

	private String trim(String text, int maxWidth) {
		while (text.length() > 1 && font.width(text) > maxWidth) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	private PwGuiTextures resourceIcon() {
		return switch (menu.resourceMode()) {
			case ITEM -> PwGuiTextures.RES_ITEM;
			case ENERGY -> PwGuiTextures.RES_ENERGY;
			case FLUID -> PwGuiTextures.RES_FLUID;
			case XP -> PwGuiTextures.RES_XP;
		};
	}

	private PwGuiTextures transferIcon() {
		return switch (TransferMode.values()[menu.transferMode()]) {
			case PRIORITY -> PwGuiTextures.TR_PRIORITY;
			case ROUND_ROBIN -> PwGuiTextures.TR_ROUND;
			case LOAD_BALANCED -> PwGuiTextures.TR_BALANCED;
		};
	}

	private Component resourceName() {
		return Component.literal(switch (menu.resourceMode()) {
			case ITEM -> "item";
			case ENERGY -> "FE";
			case FLUID -> "fluid";
			case XP -> "xp";
		});
	}

	private Component transferName() {
		return Component.literal(switch (TransferMode.values()[menu.transferMode()]) {
			case PRIORITY -> "priority";
			case ROUND_ROBIN -> "round";
			case LOAD_BALANCED -> "balanced";
		});
	}

	private Component redstoneTip() {
		return switch (menu.blockEntity().redstoneMode()) {
			case ALWAYS -> Component.translatable("gui.plinthworks.redstone.always.tip");
			case PAUSE_WHEN_POWERED -> Component.translatable("gui.plinthworks.redstone.pause.tip");
			case RUN_WHEN_POWERED -> Component.translatable("gui.plinthworks.redstone.run.tip");
		};
	}

	private static String compact(int value) {
		return value < 1000 ? String.valueOf(value) : value / 1000 + "k";
	}

	// item/exact/mod seals get the ghost-slot editor; tag/component stay on the display-capture screen
	private void openSeal(int index) {
		if (menu.seal(index).getItem() instanceof dev.cinderworks.plinthworks.item.SealItem seal) {
			switch (seal.type()) {
				case ITEM, ITEM_EXACT, MOD -> send(70 + index);
				default -> minecraft.setScreen(new SealConfigScreen(this, menu, index));
			}
		}
	}

	private void send(int id) {
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
	}

	private static String title(String value) {
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
