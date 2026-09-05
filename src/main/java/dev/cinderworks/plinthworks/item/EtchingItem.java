package dev.cinderworks.plinthworks.item;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class EtchingItem extends Item
{
	private final EtchingType type;
	private final EtchingTier tier;

	public EtchingItem(EtchingType type, EtchingTier tier) {
		super(new Properties().stacksTo(16));
		this.type = type;
		this.tier = tier;
	}

	public EtchingType type() {
		return type;
	}

	public EtchingTier tier() {
		return tier;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable(Plinthworks.MODID + ".etching."
				+ type.name().toLowerCase() + ".desc").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("plinthworks.etching.tier", tier.level())
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
