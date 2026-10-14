package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.component.MatchKey;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.RedstoneMode;
import dev.cinderworks.plinthworks.logic.network.PlinthNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class PlaytestGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);
	private static final BlockPos WEST = POS.west();
	private static final BlockPos EAST = POS.east();
	private static final BlockPos UP = POS.above();
	private static final int WAIT = 24;

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void breakerLeavesWaterAlone(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.BREAKER);
		helper.setBlock(WEST, Blocks.CHEST);
		helper.setBlock(UP, Blocks.WATER);
		helper.setBlock(EAST, Blocks.DIRT);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, !helper.getBlockState(EAST).is(Blocks.DIRT), "the breaker never ran");
			Support.expect(helper, helper.getBlockState(UP).is(Blocks.WATER), "the breaker removed the water: " + helper.getBlockState(UP));
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void breakerLeavesLavaAlone(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.BREAKER);
		helper.setBlock(WEST, Blocks.CHEST);
		helper.setBlock(UP, Blocks.LAVA);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, helper.getBlockState(UP).is(Blocks.LAVA), "the breaker removed the lava: " + helper.getBlockState(UP));
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void breakerSparesBedrockAndStorage(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.BREAKER);
		helper.setBlock(UP, Blocks.BEDROCK);
		helper.setBlock(EAST, Blocks.CHEST);
		helper.setBlock(POS.north(), Blocks.BARREL);
		helper.setBlock(POS.south(), Blocks.SPAWNER);
		Support.container(helper, EAST).setItem(0, new ItemStack(Items.DIAMOND, 3));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, helper.getBlockState(UP).is(Blocks.BEDROCK), "bedrock broke");
			Support.expect(helper, helper.getBlockState(EAST).is(Blocks.CHEST), "the chest broke");
			Support.expect(helper, helper.getBlockState(POS.north()).is(Blocks.BARREL), "the barrel broke");
			Support.expect(helper, helper.getBlockState(POS.south()).is(Blocks.SPAWNER), "the spawner broke");
			Support.expectEquals(helper, 3, Support.count(Support.container(helper, EAST), Items.DIAMOND), "diamonds");
		}).thenSucceed();
	}

	// config is global, so this runs in a batch of its own instead of beside the other breakers
	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = "config")
	public static void breakerBreaksStorageWhenToggled(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.BREAKER);
		helper.setBlock(UP, Blocks.BARREL);
		helper.setBlock(WEST, Blocks.CHEST);
		ModConfig.BREAK_BLOCK_ENTITIES.set(true);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			ModConfig.BREAK_BLOCK_ENTITIES.set(false);
			Support.expect(helper, helper.getBlockState(UP).isAir(), "the barrel survived with the toggle on");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void harvesterCutsRipeWheatAndReplants(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.HARVESTER);
		helper.setBlock(WEST, Blocks.CHEST);
		Support.container(helper, WEST).setItem(0, new ItemStack(Items.WHEAT_SEEDS, 1));
		helper.setBlock(EAST.below(), Blocks.FARMLAND);
		helper.setBlock(EAST, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			var state = helper.getBlockState(EAST);
			Support.expect(helper, state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == 0, "not replanted: " + state);
			Support.expect(helper, Support.count(Support.container(helper, WEST), Items.WHEAT) == 1, "no wheat in the chest");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void harvesterLeavesUnripeAndNonCrops(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.HARVESTER);
		helper.setBlock(WEST, Blocks.CHEST);
		helper.setBlock(EAST.below(), Blocks.FARMLAND);
		helper.setBlock(EAST, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 3));
		BlockPos cactus = POS.north().east();
		BlockPos bamboo = POS.south().west();
		helper.setBlock(cactus.below(), Blocks.SAND);
		helper.setBlock(cactus, Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, 15));
		helper.setBlock(bamboo.below(), Blocks.DIRT);
		helper.setBlock(bamboo, Blocks.BAMBOO.defaultBlockState().setValue(net.minecraft.world.level.block.BambooStalkBlock.AGE, 1));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, helper.getBlockState(EAST).getValue(CropBlock.AGE) == 3, "unripe wheat was cut");
			Support.expect(helper, helper.getBlockState(cactus).is(Blocks.CACTUS), "the cactus was harvested");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void planterSowsSeedsOnFarmland(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.PLANTER);
		helper.setBlock(WEST, Blocks.CHEST);
		Support.container(helper, WEST).setItem(0, new ItemStack(Items.WHEAT_SEEDS, 4));
		helper.setBlock(EAST.below(), Blocks.FARMLAND);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, helper.getBlockState(EAST).is(Blocks.WHEAT), "nothing planted: " + helper.getBlockState(EAST));
			Support.expectEquals(helper, 3, Support.count(Support.container(helper, WEST), Items.WHEAT_SEEDS), "seeds left");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void placerSetsBlocksFromItsBuffer(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.PLACER);
		be.display().setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 3));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			int placed = 0;
			for (int x = 1; x <= 3; x++) {
				for (int y = 0; y <= 2; y++) {
					for (int z = 1; z <= 3; z++) {
						placed += helper.getBlockState(new BlockPos(x, y, z)).is(Blocks.COBBLESTONE) ? 1 : 0;
					}
				}
			}
			Support.expect(helper, placed > 0, "nothing placed");
			Support.expectEquals(helper, 3 - placed, be.getDisplayedItem().getCount(), "buffer count vs placed " + placed);
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void magnetPullsDroppedItems(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.MAGNET);
		helper.setBlock(WEST, Blocks.CHEST);
		be.seals().setStackInSlot(0, Support.seal(SealType.ITEM, false, new MatchKey.ItemKey(ResourceLocation.withDefaultNamespace("dirt"))));
		helper.spawnItem(Items.COBBLESTONE, 2f, 2f, 2f);
		helper.spawnItem(Items.DIRT, 2f, 2f, 2f);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, 1, Support.count(Support.container(helper, WEST), Items.COBBLESTONE), "cobble in the chest");
			Support.expectEquals(helper, 0, Support.count(Support.container(helper, WEST), Items.DIRT), "blacklisted dirt in the chest");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void rangeEtchingReachesFurther(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.IMPORT);
		BlockPos far = POS.west(3);
		helper.setBlock(far, Blocks.CHEST);
		Support.container(helper, far).setItem(0, new ItemStack(Items.COBBLESTONE, 5));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, be.getDisplayedItem().isEmpty(), "pulled from out of reach");
			be.etchings().setStackInSlot(2, Support.etching(EtchingType.RANGE, EtchingTier.T2, 1));
		}).thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, 5, be.getDisplayedItem().getCount(), "buffer with range 3");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void redstoneModesGateTheTick(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.IMPORT);
		helper.setBlock(WEST, Blocks.CHEST);
		Support.container(helper, WEST).setItem(0, new ItemStack(Items.COBBLESTONE, 64));
		helper.setBlock(UP, Blocks.REDSTONE_BLOCK);
		be.cycleRedstoneMode();
		Support.expectEquals(helper, RedstoneMode.PAUSE_WHEN_POWERED, be.redstoneMode(), "mode");
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, be.getDisplayedItem().isEmpty(), "ran while powered in pause mode");
			be.cycleRedstoneMode();
		}).thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, !be.getDisplayedItem().isEmpty(), "did not run while powered in run mode");
			be.display().setStackInSlot(0, ItemStack.EMPTY);
			helper.setBlock(UP, Blocks.AIR);
		}).thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, be.getDisplayedItem().isEmpty(), "ran while unpowered in run mode");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void energyImportAndExportMoveFE(GameTestHelper helper) {
		PlinthBlockEntity in = Support.plinth(helper, POS);
		Support.fit(in, SigilType.IMPORT);
		in.cycleResourceMode();
		EnergyStorage source = PlaytestSupport.battery(helper, WEST, 50000);
		PlinthBlockEntity out = Support.plinth(helper, new BlockPos(2, 1, 8));
		Support.fit(out, SigilType.EXPORT);
		out.cycleResourceMode();
		EnergyStorage sink = PlaytestSupport.battery(helper, new BlockPos(3, 1, 8), 0);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, in.energy().getEnergyStored() > 0, "nothing imported");
			Support.expectEquals(helper, 50000 - in.energy().getEnergyStored(), source.getEnergyStored(), "energy conserved on import");
			out.energy().receiveEnergy(20000, false);
		}).thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, sink.getEnergyStored() > 0, "nothing exported");
			Support.expectEquals(helper, 20000, sink.getEnergyStored() + out.energy().getEnergyStored(), "energy conserved on export");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void plinthLoadedWithAChannelJoinsTheNetwork(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		CompoundTag tag = be.saveWithFullMetadata(Support.registries(helper));
		tag.putString("Channel", "gt_pasted");
		helper.setBlock(POS, Blocks.AIR);
		helper.setBlock(POS, dev.cinderworks.plinthworks.registry.ModBlocks.PLINTH.get());
		BlockEntity fresh = helper.getBlockEntity(POS);
		fresh.loadWithComponents(tag, Support.registries(helper));
		helper.startSequence().thenIdle(3).thenExecute(() -> {
			var members = PlinthNetwork.get(helper.getLevel()).channel("gt_pasted").members();
			Support.expect(helper, members.contains(helper.absolutePos(POS)), "a plinth restored with a channel is not a member: " + members);
		}).thenSucceed();
	}
}
