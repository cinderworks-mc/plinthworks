package dev.cinderworks.plinthworks.net;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public record SetChannelPayload(String name) implements CustomPacketPayload
{
	public static final int MAX_LENGTH = 32;

	public static final CustomPacketPayload.Type<SetChannelPayload> TYPE =
			new CustomPacketPayload.Type<>(Plinthworks.id("set_channel"));

	// wider than MAX_LENGTH so a long name gets trimmed instead of dropping the connection
	public static final StreamCodec<RegistryFriendlyByteBuf, SetChannelPayload> STREAM_CODEC =
			StreamCodec.composite(ByteBufCodecs.stringUtf8(256), SetChannelPayload::name, SetChannelPayload::new);

	// empty means nothing usable, the handler ignores it
	public static String sanitize(String raw) {
		String value = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
		return value.substring(0, Math.min(value.length(), MAX_LENGTH));
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
