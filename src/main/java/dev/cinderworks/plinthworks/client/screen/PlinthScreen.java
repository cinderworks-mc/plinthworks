package dev.cinderworks.plinthworks.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.client.PwMotion;
import dev.cinderworks.plinthworks.item.SigilType;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import dev.cinderworks.plinthworks.logic.network.TransferMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlinthScreen extends AbstractContainerScreen<PlinthMenu>
{
	private static final ResourceLocation TEXTURE =
			Plinthworks.id("textures/gui/container/plinth.png");

	// the slab is 178 wide, the readout plate hangs past its right edge
	private static final int SLAB_WIDTH = 178;
	private static final int GAUGE_X = 9;
	private static final int GAUGE_Y = 62;
	private static final int SOCKET_X = 7;
	private static final int SOCKET_Y = 26;
	private static final int TOOLBAR_X = 127;
	private static final int TOOLBAR_Y = 19;
	private static final int COMPACT_BELOW = 250;

	private final PwGauge gauge = new PwGauge();
	private final PwStatPlate plate = new PwStatPlate();
	private final RandomSource random = RandomSource.create();
	private PwIconButton transferButton;
	private int age;
	private int flickerUntil;
	private int nextFlicker = 24;

	public PlinthScreen(PlinthMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 198;
		imageHeight = 214;
		titleLabelX = 13;
		titleLabelY = 5;
		inventoryLabelX = 8;
		inventoryLabelY = 123;
	}

	@Override
	protected void init() {
		super.init();
		for (int i = 0; i < menu.blockEntity().seals().getSlots(); i++) {
			int index = i;
			addRenderableWidget(new PwSealTag(leftPos + 63 + i * 20, topPos + 84, i + 1, () -> openSeal(index)));
		}
		int tx = leftPos + TOOLBAR_X;
		int ty = topPos + TOOLBAR_Y;
		addRenderableWidget(new PwIconButton(tx, ty, this::resourceIcon, this::resourceTip, () -> send(80)));
		transferButton = addRenderableWidget(new PwIconButton(tx + 17, ty, this::transferIcon, this::transferTip, () -> send(20)));
		addRenderableWidget(new PwIconButton(tx + 34, ty, () -> PwGuiTextures.REDSTONE, this::redstoneTip,
				() -> menu.blockEntity().redstoneMode() != dev.cinderworks.plinthworks.logic.RedstoneMode.ALWAYS,
				() -> send(60)));
		addRenderableWidget(new PwIconButton(tx + 51, ty, () -> PwGuiTextures.NETWORK,
				() -> brass(Component.translatable("gui.plinthworks.network_overview")),
				() -> minecraft.setScreen(new NetworkOverviewScreen(this, menu))));
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		age++;
		gauge.tick(menu.resourceMode(), gaugeFraction());
		if (age >= nextFlicker) {
			flickerUntil = age + 3;
			nextFlicker = age + 20 + random.nextInt(16);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		updatePlate();
		// a plinth on no channel has nobody to share with, so there is no mode to pick
		transferButton.active = hasChannel();
		super.render(graphics, mouseX, mouseY, partialTick);
		if (hoveredSlot != null && hoveredSlot.isActive()) {
			graphics.renderOutline(leftPos + hoveredSlot.x - 1, topPos + hoveredSlot.y - 1, 18, 18, 0xFFD2A55A);
		}
		renderGaugeTooltip(graphics, mouseX, mouseY);
		renderPlateTooltip(graphics, mouseX, mouseY);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	public int getSlotColor(int index) {
		return 0x30B38A4A;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.enableBlend();
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, SLAB_WIDTH, imageHeight, 256, 256);
		if (menu.verb() != 0) {
			PwThread.renderLit(graphics, leftPos, topPos, age);
			boolean dim = !PwMotion.reduced() && age < flickerUntil;
			if (dim) {
				graphics.setColor(0.8F, 0.8F, 0.8F, 1F);
			}
			PwGuiTextures.SOCKET_LIT.render(graphics, leftPos + SOCKET_X, topPos + SOCKET_Y);
			graphics.setColor(1F, 1F, 1F, 1F);
		}
		renderHints(graphics);
		if (menu.resourceMode() != ResourceMode.ITEM) {
			gauge.render(graphics, leftPos + GAUGE_X, topPos + GAUGE_Y, menu.resourceMode(),
					menu.blockEntity().fluid().getFluid(), partialTick);
			capGlyph().render(graphics, leftPos + 17, topPos + 55);
		}
		plate.renderBg(graphics, leftPos, topPos);
	}

	// a faint shape in each empty slot says what goes there
	private void renderHints(GuiGraphics graphics) {
		int etchings = menu.blockEntity().etchings().getSlots();
		int seals = menu.blockEntity().seals().getSlots();
		for (int i = 0; i < 1 + etchings + seals && i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			if (slot.hasItem()) {
				continue;
			}
			int x = leftPos + slot.x;
			int y = topPos + slot.y;
			if (i == 0) {
				PwGuiTextures.HINT_SIGIL.render(graphics, x + 3, y + 3);
			} else if (i <= etchings) {
				PwGuiTextures.HINT_ETCH.render(graphics, x + 5, y + 4);
			} else {
				PwGuiTextures.HINT_SEAL.render(graphics, x + 4, y + 4);
			}
		}
	}

	private PwGuiTextures capGlyph() {
		return switch (menu.resourceMode()) {
			case ENERGY -> PwGuiTextures.CAP_ENERGY;
			case XP -> PwGuiTextures.CAP_XP;
			default -> PwGuiTextures.CAP_FLUID;
		};
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
		if (mouseX < fx || mouseX >= fx + PwGauge.WIDTH || mouseY < fy || mouseY >= fy + PwGauge.HEIGHT) {
			return;
		}
		graphics.renderComponentTooltip(font, gaugeTooltip(), mouseX, mouseY);
	}

	private List<Component> gaugeTooltip() {
		int percent = Math.round(gaugeFraction() * 100);
		List<Component> lines = new ArrayList<>();
		switch (menu.resourceMode()) {
			case ENERGY -> lines.add(brass(Component.translatable("gui.plinthworks.stat_energy",
					number(menu.energyStored()), number(menu.energyCapacity()))));
			case XP -> lines.add(brass(Component.translatable("gui.plinthworks.stat_xp",
					number(menu.fluidStored() / Math.max(1, menu.xpMbPerPoint())), number(menu.fluidStored()))));
			default -> {
				lines.add(brass(fluidName()));
				lines.add(Component.translatable("gui.plinthworks.stat_fluid",
						number(menu.fluidStored()), number(menu.fluidCapacity())));
			}
		}
		lines.add(Component.literal(percent + "%").withStyle(style -> style.withColor(PwGui.MUTED)));
		return lines;
	}

	private static String number(int value) {
		return NumberFormat.getIntegerInstance().format(value);
	}

	private Component fluidName() {
		FluidStack fluid = menu.blockEntity().fluid().getFluid();
		return fluid.isEmpty()
				? Component.translatable("gui.plinthworks.fluid_empty")
				: fluid.getHoverName();
	}

	private void renderPlateTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		Component tip = plate.tipAt(mouseX - leftPos, mouseY - topPos);
		if (tip != null) {
			graphics.renderTooltip(font, tip, mouseX, mouseY);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, PwGui.TITLE, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.sigil"), 9, 17, PwGui.LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.etchings"), 49, 17, PwGui.LABEL, false);
		graphics.drawString(font, Component.translatable("gui.plinthworks.seals"), 61, 57, PwGui.LABEL, false);
		if (menu.resourceMode() == ResourceMode.ITEM) {
			graphics.drawString(font, Component.translatable("gui.plinthworks.display"), 9, 53, PwGui.LABEL, false);
		} else {
			drawGaugeValue(graphics);
		}
		if (height >= COMPACT_BELOW) {
			graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, PwGui.MUTED, false);
		}
		plate.renderText(graphics, font);
	}

	private void drawGaugeValue(GuiGraphics graphics) {
		int x = GAUGE_X + 24;
		// an empty tube shows its hatching, the number just goes quiet
		graphics.drawString(font, compact(gaugeStored()), x, GAUGE_Y + 4,
				gaugeStored() == 0 ? PwGui.MUTED : PwGui.VALUE, false);
		graphics.drawString(font, Component.literal(gaugeUnit()), x, GAUGE_Y + 14, PwGui.MUTED, false);
	}

	private void updatePlate() {
		int value = menu.verb();
		boolean active = value != 0;
		String verb = active ? SigilType.values()[value - 1].name().toLowerCase(Locale.ROOT) : "idle";
		List<PwStatPlate.Row> rows = new ArrayList<>();
		rows.add(new PwStatPlate.Row(PwGuiTextures.STAT_RATE,
				Component.translatable("gui.plinthworks.val_ticks", menu.intervalTicks()),
				Component.translatable("gui.plinthworks.stat_interval", menu.intervalTicks())));
		rows.add(new PwStatPlate.Row(PwGuiTextures.STAT_MOVE, moveValue(), moveTip()));
		if (menu.resourceMode() == ResourceMode.ITEM) {
			rows.add(new PwStatPlate.Row(PwGuiTextures.STAT_HOLD, Component.literal(String.valueOf(menu.bufferSlots())),
					Component.translatable("gui.plinthworks.stat_storage", menu.bufferSlots())));
		}
		rows.add(new PwStatPlate.Row(PwGuiTextures.STAT_REACH, Component.literal(String.valueOf(menu.range())),
				Component.translatable("gui.plinthworks.stat_range", menu.range())));
		rows.add(channelRow());
		plate.update(font, Component.translatable("gui.plinthworks.verb." + verb), active, rows);
	}

	private PwStatPlate.Row channelRow() {
		String channel = menu.blockEntity().channel().isEmpty()
				? Component.translatable("gui.plinthworks.channel_local").getString() : menu.blockEntity().channel();
		int members = menu.channelMembers();
		int suffix = font.width(" (" + members + ")");
		String shown = PwStatPlate.clip(font, channel, PwStatPlate.textLimit() - suffix);
		return new PwStatPlate.Row(PwGuiTextures.STAT_CHAN,
				Component.translatable("gui.plinthworks.stat_channel", shown, members),
				Component.translatable("gui.plinthworks.stat_channel", channel, members));
	}

	private Component moveValue() {
		return switch (menu.resourceMode()) {
			case ITEM -> Component.literal(String.valueOf(menu.throughput()));
			case ENERGY -> Component.translatable("gui.plinthworks.val_fe", menu.energyThroughput());
			case FLUID, XP -> Component.translatable("gui.plinthworks.val_mb", menu.fluidThroughput());
		};
	}

	private Component moveTip() {
		return switch (menu.resourceMode()) {
			case ITEM -> Component.translatable("gui.plinthworks.stat_throughput", menu.throughput());
			case ENERGY -> Component.translatable("gui.plinthworks.stat_energy_move", menu.energyThroughput());
			case FLUID, XP -> Component.translatable("gui.plinthworks.stat_fluid_move", menu.fluidThroughput());
		};
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

	private Component resourceTip() {
		return withNext(Component.translatable("gui.plinthworks.resource.tip.state", resourceName(menu.resourceMode())),
				resourceName(menu.resourceMode().next()));
	}

	private boolean hasChannel() {
		return !menu.blockEntity().channel().isEmpty();
	}

	private Component transferTip() {
		if (!hasChannel()) {
			return Component.empty().append(brass(Component.translatable("gui.plinthworks.mode.tip.no_channel"))).append("\n")
					.append(Component.translatable("gui.plinthworks.mode.tip.name_one").withStyle(style -> style.withColor(PwGui.MUTED)));
		}
		TransferMode mode = TransferMode.values()[menu.transferMode()];
		return withNext(Component.translatable("gui.plinthworks.mode.tip.state", transferName(mode)),
				transferName(mode.next()));
	}

	// the current mode in brass, then what a click switches to
	private static Component withNext(MutableComponent current, Component next) {
		return Component.empty().append(brass(current)).append("\n")
				.append(Component.translatable("gui.plinthworks.click_for", next).withStyle(style -> style.withColor(PwGui.MUTED)));
	}

	private static MutableComponent brass(Component text) {
		return text.copy().withStyle(style -> style.withColor(PwGui.BRASS));
	}

	private static Component resourceName(ResourceMode mode) {
		return Component.translatable("gui.plinthworks.resource." + mode.name().toLowerCase(Locale.ROOT));
	}

	private static Component transferName(TransferMode mode) {
		return Component.translatable("gui.plinthworks.transfer." + mode.name().toLowerCase(Locale.ROOT));
	}

	private Component redstoneTip() {
		return brass(switch (menu.blockEntity().redstoneMode()) {
			case ALWAYS -> Component.translatable("gui.plinthworks.redstone.always.tip");
			case PAUSE_WHEN_POWERED -> Component.translatable("gui.plinthworks.redstone.pause.tip");
			case RUN_WHEN_POWERED -> Component.translatable("gui.plinthworks.redstone.run.tip");
		});
	}

	private static String compact(int value) {
		if (value < 1000) {
			return String.valueOf(value);
		}
		return value < 1_000_000 ? value / 1000 + "k" : value / 1_000_000 + "M";
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
}
