package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.PlinthBlock;
import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.resource.EssentiaSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class VisualGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void idlePlinthStaysDark(GameTestHelper helper) {
		Support.plinth(helper, POS);
		helper.startSequence().thenExecuteAfter(3, () -> expectState(helper, false, 0, 0)).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void glowFollowsTheSigil(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		helper.startSequence()
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 0))
				.thenExecute(() -> be.sigil().setStackInSlot(0, Support.sigil(SigilType.VOID)))
				.thenExecuteAfter(2, () -> expectState(helper, true, 0, 0))
				.thenExecute(() -> be.sigil().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY))
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 0))
				.thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void glyphFollowsTheResourceMode(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		helper.startSequence()
				.thenExecute(be::cycleResourceMode)
				.thenExecuteAfter(2, () -> expectState(helper, false, 1, 0))
				.thenExecute(be::cycleResourceMode)
				.thenExecuteAfter(2, () -> expectState(helper, false, 2, 0))
				.thenExecute(be::cycleResourceMode)
				.thenExecuteAfter(2, () -> expectState(helper, false, 3, 0))
				// essentia is only in the cycle when a compat can move it
				.thenExecute(() -> {
					if (EssentiaSupport.available()) {
						be.cycleResourceMode();
					}
				})
				.thenExecuteAfter(2, () -> expectState(helper, false, EssentiaSupport.available() ? 4 : 3, 0))
				.thenExecute(be::cycleResourceMode)
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 0))
				.thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void gaugeCountsEtchings(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		helper.startSequence()
				.thenExecute(() -> be.etchings().setStackInSlot(0, Support.etching(EtchingType.SPEED, EtchingTier.T1, 1)))
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 1))
				.thenExecute(() -> be.etchings().setStackInSlot(1, Support.etching(EtchingType.RANGE, EtchingTier.T1, 1)))
				.thenExecute(() -> be.etchings().setStackInSlot(3, Support.etching(EtchingType.STORAGE, EtchingTier.T1, 1)))
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 3))
				.thenExecute(() -> be.etchings().setStackInSlot(2, Support.etching(EtchingType.CAPACITY, EtchingTier.T1, 1)))
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 4))
				.thenExecute(() -> be.etchings().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY))
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 3))
				.thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void glowFollowsRedstone(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		be.sigil().setStackInSlot(0, Support.sigil(SigilType.VOID));
		be.cycleRedstoneMode();
		BlockPos power = POS.east();
		helper.startSequence()
				.thenExecuteAfter(2, () -> expectState(helper, true, 0, 0))
				.thenExecute(() -> helper.setBlock(power, Blocks.REDSTONE_BLOCK))
				.thenExecuteAfter(2, () -> expectState(helper, false, 0, 0))
				.thenExecute(() -> helper.setBlock(power, Blocks.AIR))
				.thenExecuteAfter(2, () -> expectState(helper, true, 0, 0))
				.thenSucceed();
	}

	private static void expectState(GameTestHelper helper, boolean active, int mode, int gauge) {
		BlockState state = helper.getBlockState(POS);
		Support.expectEquals(helper, active, state.getValue(PlinthBlock.ACTIVE), "active");
		Support.expectEquals(helper, mode, state.getValue(PlinthBlock.MODE), "mode");
		Support.expectEquals(helper, gauge, state.getValue(PlinthBlock.GAUGE), "gauge");
	}
}
