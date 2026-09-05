package dev.hartforge.plinthworks.item;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.component.SealConfig;
import dev.hartforge.plinthworks.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class SealItem extends Item
{
	private final SealType type;

	public SealItem(SealType type) {
		super(new Properties().stacksTo(16).component(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY));
		this.type = type;
	}

	public SealType type() {
		return type;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable(Plinthworks.MODID + ".seal."
				+ type.name().toLowerCase() + ".desc").withStyle(ChatFormatting.GRAY));
		SealConfig config = stack.getOrDefault(ModDataComponents.SEAL_CONFIG.get(), SealConfig.EMPTY);
		tooltip.add(Component.translatable(config.whitelist() ? "plinthworks.seal.whitelist"
				: "plinthworks.seal.blacklist", config.keys().size()).withStyle(ChatFormatting.DARK_GRAY));
	}
}
