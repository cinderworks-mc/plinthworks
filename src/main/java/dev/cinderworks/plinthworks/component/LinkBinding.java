package dev.cinderworks.plinthworks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;

import java.util.Optional;

public record LinkBinding(String channel, Optional<GlobalPos> anchor)
{
	public static final LinkBinding EMPTY = new LinkBinding("", Optional.empty());
	public static final Codec<LinkBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("channel").forGetter(LinkBinding::channel),
			GlobalPos.CODEC.optionalFieldOf("anchor").forGetter(LinkBinding::anchor)
	).apply(instance, LinkBinding::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, LinkBinding> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
