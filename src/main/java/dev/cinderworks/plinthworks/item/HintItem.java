package dev.cinderworks.plinthworks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class HintItem extends Item
{
	private final String[] lines;

	public HintItem(Properties properties, String... lines) {
		super(properties);
		this.lines = lines;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		for (String line : lines) {
			tooltip.add(Component.translatable(line).withStyle(ChatFormatting.GRAY));
		}
	}
}
