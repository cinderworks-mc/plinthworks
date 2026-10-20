package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.network.PlinthNetwork;
import dev.cinderworks.plinthworks.logic.network.TransferMode;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class NetworkGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);
	private static final BlockPos GONE = new BlockPos(5, 1, 5);
	private static final BlockPos SECOND = new BlockPos(2, 1, 8);

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, timeoutTicks = 300)
	public static void staleChannelMembersArePrunedEveryTwoHundredTicks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlinthBlockEntity be = Support.plinth(helper, POS);
		be.setChannel("gt_prune");
		BlockPos stale = helper.absolutePos(GONE);
		// after this call the next prune is due 200 ticks on at the earliest
		PlinthNetwork.get(level);
		PlinthNetwork.get(level).add("gt_prune", stale);
		Support.expect(helper, PlinthNetwork.get(level).channel("gt_prune").members().contains(stale),
				"the gate opened again straight away");
		helper.startSequence().thenIdle(205).thenExecute(() -> {
			var members = PlinthNetwork.get(level).channel("gt_prune").members();
			Support.expect(helper, !members.contains(stale), "the stale member was never pruned");
			Support.expect(helper, members.contains(helper.absolutePos(POS)), "the live plinth was pruned");
		}).thenSucceed();
	}

	// the screen greys the transfer button out on a plinth with no channel, this is the server side of that
	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void transferButtonOnlyCyclesOnAChannel(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		var player = Support.player(helper);
		var menu = new PlinthMenu(1, player.getInventory(), be);
		Support.expect(helper, !menu.clickMenuButton(player, 20), "the transfer button did something with no channel");
		String name = "gt_cycle_" + System.nanoTime() % 1000000;
		be.setChannel(name);
		Support.expect(helper, menu.clickMenuButton(player, 20), "the transfer button was ignored on a channel");
		Support.expectEquals(helper, TransferMode.ROUND_ROBIN, PlinthNetwork.get(helper.getLevel()).channel(name).mode(),
				"mode after one click");
		helper.succeed();
	}

	// the channel screen lists what the menu was sent, so a rename or a new member has to reach an open menu
	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void openMenuGetsTheMembersWhenTheChannelChanges(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		PlinthBlockEntity other = Support.plinth(helper, SECOND);
		var player = Support.player(helper);
		var menu = new PlinthMenu(1, player.getInventory(), be);
		var client = new PlinthMenu(1, player.getInventory(), helper.absolutePos(POS));
		Support.expect(helper, menu.memberUpdate() == null, "a member update with no channel");
		String name = "gt_members_" + System.nanoTime() % 1000000;
		be.setChannel(name);
		receive(helper, client, menu.memberUpdate());
		Support.expectEquals(helper, java.util.List.of(helper.absolutePos(POS)), client.networkMembers(), "members after naming");
		Support.expect(helper, menu.memberUpdate() == null, "the same members were sent twice");
		other.setChannel(name);
		receive(helper, client, menu.memberUpdate());
		Support.expectEquals(helper, 2, client.networkMembers().size(), "members after a second plinth joined");
		String renamed = name + "_b";
		menu.applyChannel(renamed);
		Support.expectEquals(helper, java.util.List.of(helper.absolutePos(POS)), menu.networkMembers(),
				"members sent after a rename");
		helper.succeed();
	}

	// what the client menu ends up with, through the real payload codec
	private static void receive(GameTestHelper helper, PlinthMenu client, dev.cinderworks.plinthworks.net.MembersPayload update) {
		Support.expect(helper, update != null, "no member update was made");
		var buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
		dev.cinderworks.plinthworks.net.MembersPayload.STREAM_CODEC.encode(buf, update);
		client.setNetworkMembers(dev.cinderworks.plinthworks.net.MembersPayload.STREAM_CODEC.decode(buf).members());
	}
}
