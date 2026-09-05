package dev.hartforge.plinthworks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PlinthLoadout(ItemStack sigil, List<ItemStack> etchings, List<ItemStack> seals)
{
	public static final Codec<PlinthLoadout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.OPTIONAL_CODEC.fieldOf("sigil").forGetter(PlinthLoadout::sigil),
			ItemStack.OPTIONAL_CODEC.listOf().fieldOf("etchings").forGetter(PlinthLoadout::etchings),
			ItemStack.OPTIONAL_CODEC.listOf().fieldOf("seals").forGetter(PlinthLoadout::seals)
	).apply(instance, PlinthLoadout::new));

	public PlinthLoadout {
		sigil = sigil.copy();
		etchings = etchings.stream().map(ItemStack::copy).toList();
		seals = seals.stream().map(ItemStack::copy).toList();
	}
}
