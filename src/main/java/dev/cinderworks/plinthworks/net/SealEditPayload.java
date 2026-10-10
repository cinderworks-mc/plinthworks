package dev.cinderworks.plinthworks.net;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SealEditPayload(SealEdit action, int seal, int key) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<SealEditPayload> TYPE =
			new CustomPacketPayload.Type<>(Plinthworks.id("seal_edit"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SealEditPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.idMapper(SealEdit::fromOrdinal, SealEdit::ordinal), SealEditPayload::action,
					ByteBufCodecs.VAR_INT, SealEditPayload::seal,
					ByteBufCodecs.VAR_INT, SealEditPayload::key,
					SealEditPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
