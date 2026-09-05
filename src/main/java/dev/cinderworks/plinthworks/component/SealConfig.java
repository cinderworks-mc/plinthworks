package dev.cinderworks.plinthworks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;

import java.util.List;

public record SealConfig(boolean whitelist, List<MatchKey> keys)
{
	public static final SealConfig EMPTY = new SealConfig(true, List.of());
	public static final Codec<SealConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("whitelist").forGetter(SealConfig::whitelist),
			MatchKey.CODEC.listOf().fieldOf("keys").forGetter(SealConfig::keys)
	).apply(instance, SealConfig::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, SealConfig> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public SealConfig {
		keys = List.copyOf(keys);
	}

	public SealConfig toggleMode() {
		return new SealConfig(!whitelist, keys);
	}

	public SealConfig with(MatchKey key) {
		if (keys.contains(key)) {
			return this;
		}
		List<MatchKey> next = new java.util.ArrayList<>(keys);
		next.add(key);
		return new SealConfig(whitelist, next);
	}
}
