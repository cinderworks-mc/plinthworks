package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.network.PlinthNetwork;
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
}
