package dev.hartforge.plinthworks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record PlinthContents(ResourceLocation baseBlock)
{
	public static final Codec<PlinthContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("base_block").forGetter(PlinthContents::baseBlock)
	).apply(instance, PlinthContents::new));
}
