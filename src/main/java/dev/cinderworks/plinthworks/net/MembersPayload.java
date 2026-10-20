package dev.cinderworks.plinthworks.net;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

// the channel's member list for an open plinth menu, sent again whenever it changes
public record MembersPayload(int containerId, List<BlockPos> members) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<MembersPayload> TYPE =
			new CustomPacketPayload.Type<>(Plinthworks.id("members"));

	public static final StreamCodec<RegistryFriendlyByteBuf, MembersPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, MembersPayload::containerId,
					BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), MembersPayload::members,
					MembersPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
