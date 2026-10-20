package dev.cinderworks.plinthworks.logic.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

// whole points of one aspect. the aspect stays a plain id so nothing here needs the mod that defines it
public record Essentia(ResourceLocation aspect, int amount)
{
	public static final Codec<Essentia> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("aspect").forGetter(Essentia::aspect),
			Codec.INT.fieldOf("amount").forGetter(Essentia::amount)
	).apply(instance, Essentia::new));
}
