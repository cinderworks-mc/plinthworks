package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.component.MatchKey;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.item.SealType;
import dev.cinderworks.plinthworks.item.SigilType;
import dev.cinderworks.plinthworks.logic.PlinthTick;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class TransferGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);
	private static final BlockPos WEST = POS.west();
	private static final BlockPos EAST = POS.east();
	private static final int WAIT = 24;

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void importPullsFromAnAdjacentContainer(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.IMPORT);
		helper.setBlock(WEST, Blocks.CHEST);
		Support.container(helper, WEST).setItem(0, new ItemStack(Items.COBBLESTONE, 10));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectStack(helper, new ItemStack(Items.COBBLESTONE, 10), be.getDisplayedItem(), "plinth buffer");
			Support.expectEquals(helper, 0, Support.count(Support.container(helper, WEST), Items.COBBLESTONE), "items left in the chest");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void exportPushesIntoAdjacentContainers(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.EXPORT);
		helper.setBlock(WEST, Blocks.CHEST);
		helper.setBlock(EAST, Blocks.BARREL);
		be.display().setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 10));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, be.getDisplayedItem().isEmpty(), "plinth still holds " + be.getDisplayedItem());
			int moved = Support.count(Support.container(helper, WEST), Items.COBBLESTONE)
					+ Support.count(Support.container(helper, EAST), Items.COBBLESTONE);
			Support.expectEquals(helper, 10, moved, "items in the containers");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void linkedPairMovesItemsAcrossAChannel(GameTestHelper helper) {
		BlockPos far = new BlockPos(10, 1, 10);
		BlockPos farChest = far.east();
		linkedPair(helper, far, "gt_pair_linked");
		helper.startSequence().thenIdle(WAIT * 2).thenExecute(() -> {
			Support.expectEquals(helper, 20, Support.count(Support.container(helper, farChest), Items.COBBLESTONE), "items at the far chest");
			Support.expectEquals(helper, 0, Support.count(Support.container(helper, WEST), Items.COBBLESTONE), "items left at the near chest");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void unlinkedPairKeepsItsItems(GameTestHelper helper) {
		BlockPos far = new BlockPos(10, 1, 10);
		BlockPos farChest = far.east();
		PlinthBlockEntity near = linkedPair(helper, far, "");
		helper.startSequence().thenIdle(WAIT * 2).thenExecute(() -> {
			Support.expectEquals(helper, 0, Support.count(Support.container(helper, farChest), Items.COBBLESTONE), "items at the far chest");
			Support.expectEquals(helper, 20, near.getDisplayedItem().getCount(), "items in the importing plinth");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void voidOnlyDrainsItsOwnBuffer(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.VOID);
		helper.setBlock(WEST, Blocks.CHEST);
		helper.setBlock(EAST, Blocks.BARREL);
		Support.container(helper, WEST).setItem(0, new ItemStack(Items.DIRT, 40));
		Support.container(helper, EAST).setItem(0, new ItemStack(Items.STONE, 40));
		be.display().setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 30));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, be.getDisplayedItem().isEmpty(), "buffer still holds " + be.getDisplayedItem());
			Support.expectEquals(helper, 40, Support.count(Support.container(helper, WEST), Items.DIRT), "dirt left in the chest");
			Support.expectEquals(helper, 40, Support.count(Support.container(helper, EAST), Items.STONE), "stone left in the barrel");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void voidLeavesANearbyTankAlone(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.VOID);
		be.cycleResourceMode();
		be.cycleResourceMode();
		helper.setBlock(WEST, Blocks.WATER_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
		IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(WEST), null);
		Support.expect(helper, tank != null && tank.getFluidInTank(0).getAmount() > 0, "the cauldron does not show up as a full tank");
		int before = tank.getFluidInTank(0).getAmount();
		be.fluid().fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, 0, be.fluid().getFluidAmount(), "fluid left in the plinth");
			Support.expectEquals(helper, before, tank.getFluidInTank(0).getAmount(), "fluid left in the cauldron");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void craftingStaysOffByDefault(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.CRAFTING);
		helper.setBlock(WEST, Blocks.CHEST);
		Support.container(helper, WEST).setItem(0, new ItemStack(Items.OAK_LOG, 5));
		be.display().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 1));
		Support.expect(helper, !ModConfig.CRAFTING_ENABLED.get(), "crafting is on in the default config");
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, 5, Support.count(Support.container(helper, WEST), Items.OAK_LOG), "logs left in the chest");
			Support.expectEquals(helper, 1, be.getDisplayedItem().getCount(), "planks in the plinth");
			// the same setup does craft when asked directly, so the idle result above means something
			Support.expect(helper, PlinthTick.craft(be, be.upgrades()), "the setup cannot craft at all");
			Support.expectEquals(helper, 4, Support.count(Support.container(helper, WEST), Items.OAK_LOG), "logs left after a direct craft");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void whitelistSealOnlyLetsItsItemThrough(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.IMPORT);
		be.seals().setStackInSlot(0, Support.seal(SealType.ITEM, true, new MatchKey.ItemKey(ResourceLocation.withDefaultNamespace("cobblestone"))));
		Container chest = chestWith(helper, new ItemStack(Items.DIRT, 10), new ItemStack(Items.STONE, 10), new ItemStack(Items.COBBLESTONE, 10));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectStack(helper, new ItemStack(Items.COBBLESTONE, 10), be.getDisplayedItem(), "plinth buffer");
			Support.expectEquals(helper, 10, Support.count(chest, Items.DIRT), "dirt left in the chest");
			Support.expectEquals(helper, 10, Support.count(chest, Items.STONE), "stone left in the chest");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void blacklistSealHoldsItsItemBack(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.IMPORT);
		be.seals().setStackInSlot(0, Support.seal(SealType.ITEM, false, new MatchKey.ItemKey(ResourceLocation.withDefaultNamespace("dirt"))));
		Container chest = chestWith(helper, new ItemStack(Items.DIRT, 10), new ItemStack(Items.COBBLESTONE, 10));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectStack(helper, new ItemStack(Items.COBBLESTONE, 10), be.getDisplayedItem(), "plinth buffer");
			Support.expectEquals(helper, 10, Support.count(chest, Items.DIRT), "dirt left in the chest");
		}).thenSucceed();
	}

	private static PlinthBlockEntity linkedPair(GameTestHelper helper, BlockPos far, String channel) {
		PlinthBlockEntity near = Support.plinth(helper, POS);
		Support.fit(near, SigilType.IMPORT);
		PlinthBlockEntity other = Support.plinth(helper, far);
		Support.fit(other, SigilType.EXPORT);
		near.setChannel(channel);
		other.setChannel(channel);
		helper.setBlock(WEST, Blocks.CHEST);
		helper.setBlock(far.east(), Blocks.CHEST);
		Support.container(helper, WEST).setItem(0, new ItemStack(Items.COBBLESTONE, 20));
		return near;
	}

	private static Container chestWith(GameTestHelper helper, ItemStack... stacks) {
		helper.setBlock(WEST, Blocks.CHEST);
		Container chest = Support.container(helper, WEST);
		for (int i = 0; i < stacks.length; i++) {
			chest.setItem(i, stacks[i]);
		}
		return chest;
	}
}
