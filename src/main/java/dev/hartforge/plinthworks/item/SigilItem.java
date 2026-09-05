package dev.hartforge.plinthworks.item;

import dev.hartforge.plinthworks.Plinthworks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class SigilItem extends Item
{
	private final SigilType type;

	public SigilItem(SigilType type) {
		super(new Properties().stacksTo(16));
		this.type = type;
	}

	public SigilType type() {
		return type;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable(Plinthworks.MODID + ".sigil."
				+ type.name().toLowerCase() + ".desc").withStyle(ChatFormatting.GRAY));
	}
}
