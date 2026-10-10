package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.item.SigilType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@PrefixGameTestTemplate(false)
public class BreakerGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);
	private static final BlockPos TARGET = POS.above();
	private static final BlockPos CHEST = POS.west();
	private static final int WAIT = 24;

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void breakerAsksTheBreakEventFirst(GameTestHelper helper) {
		AtomicInteger asked = new AtomicInteger();
		Consumer<BlockEvent.BreakEvent> watcher = event -> {
			if (event.getPos().equals(helper.absolutePos(TARGET)) && isPlinthPlayer(event)) {
				asked.incrementAndGet();
			}
		};
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class, watcher);
		setup(helper);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			NeoForge.EVENT_BUS.unregister(watcher);
			Support.expect(helper, asked.get() > 0, "no break event came from the plinth's fake player");
			Support.expect(helper, helper.getBlockState(TARGET).isAir(), "the block was not broken");
			Support.expectEquals(helper, 1, Support.count(Support.container(helper, CHEST), Items.DIRT), "drops in the chest");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void claimVetoStopsTheBreaker(GameTestHelper helper) {
		AtomicInteger vetoed = new AtomicInteger();
		Consumer<BlockEvent.BreakEvent> veto = event -> {
			if (event.getPos().equals(helper.absolutePos(TARGET)) && isPlinthPlayer(event)) {
				vetoed.incrementAndGet();
				event.setCanceled(true);
			}
		};
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class, veto);
		setup(helper);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			NeoForge.EVENT_BUS.unregister(veto);
			Support.expect(helper, vetoed.get() > 0, "the veto was never asked");
			Support.expect(helper, helper.getBlockState(TARGET).is(Blocks.DIRT), "the block broke through the veto");
			Support.expectEquals(helper, 0, Support.count(Support.container(helper, CHEST), Items.DIRT), "drops in the chest");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void fullStorageNoticeIsThrottled(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.BREAKER);
		helper.setBlock(TARGET, Blocks.DIRT);
		long[] first = new long[1];
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			first[0] = be.nextSkipNotice();
			Support.expect(helper, first[0] > 0, "the skipped block never started the notice timer");
			Support.expect(helper, helper.getBlockState(TARGET).is(Blocks.DIRT), "the block broke with nowhere to put the drops");
		}).thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, first[0], be.nextSkipNotice(), "notice timer after a second skip");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void noNoticeWhenTheBreakWorks(GameTestHelper helper) {
		setup(helper);
		PlinthBlockEntity be = (PlinthBlockEntity) helper.getBlockEntity(POS);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, helper.getBlockState(TARGET).isAir(), "the block was not broken");
			Support.expectEquals(helper, 0L, be.nextSkipNotice(), "notice timer after a clean break");
		}).thenSucceed();
	}

	private static void setup(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.BREAKER);
		helper.setBlock(CHEST, Blocks.CHEST);
		helper.setBlock(TARGET, Blocks.DIRT);
	}

	private static boolean isPlinthPlayer(BlockEvent.BreakEvent event) {
		return event.getPlayer() instanceof FakePlayer fake && fake.getGameProfile().getName().equals("[plinthworks]");
	}
}
