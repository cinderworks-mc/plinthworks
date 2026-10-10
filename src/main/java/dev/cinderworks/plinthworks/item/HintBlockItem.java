package dev.cinderworks.plinthworks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class HintBlockItem extends BlockItem
{
	private final String[] lines;

	public HintBlockItem(Block block, Properties properties, String... lines) {
		super(block, properties);
		this.lines = lines;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		for (String line : lines) {
			tooltip.add(Component.translatable(line).withStyle(ChatFormatting.GRAY));
		}
	}
}
