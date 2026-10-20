package dev.cinderworks.plinthworks.net;

import dev.cinderworks.plinthworks.menu.PlinthMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ModNetwork
{
	private static final String VERSION = "1";

	public static void register(RegisterPayloadHandlersEvent event) {
		event.registrar(VERSION)
				.playToServer(SetChannelPayload.TYPE, SetChannelPayload.STREAM_CODEC, ModNetwork::setChannel)
				.playToServer(SealEditPayload.TYPE, SealEditPayload.STREAM_CODEC, ModNetwork::sealEdit)
				.playToClient(MembersPayload.TYPE, MembersPayload.STREAM_CODEC, ModNetwork::members);
	}

	private static void setChannel(SetChannelPayload payload, IPayloadContext context) {
		PlinthMenu menu = openPlinth(context);
		if (menu == null) {
			return;
		}
		String name = SetChannelPayload.sanitize(payload.name());
		if (name.isEmpty()) {
			return;
		}
		menu.applyChannel(name);
	}

	private static void members(MembersPayload payload, IPayloadContext context) {
		if (context.player().containerMenu instanceof PlinthMenu menu && menu.containerId == payload.containerId()) {
			menu.setNetworkMembers(payload.members());
		}
	}

	private static void sealEdit(SealEditPayload payload, IPayloadContext context) {
		PlinthMenu menu = openPlinth(context);
		if (menu != null) {
			menu.applySealEdit(payload.action(), payload.seal(), payload.key());
		}
	}

	// only act on the menu the sender has open and still in reach
	private static PlinthMenu openPlinth(IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)
				|| !(player.containerMenu instanceof PlinthMenu menu)
				|| !menu.stillValid(player)) {
			return null;
		}
		return menu;
	}
}
