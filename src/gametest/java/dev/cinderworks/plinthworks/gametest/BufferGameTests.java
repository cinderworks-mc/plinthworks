package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.item.EtchingTier;
import dev.cinderworks.plinthworks.item.EtchingType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class BufferGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void displayBufferStopsAtNinetyNine(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.expectEquals(helper, 64, be.display().getSlotLimit(0), "limit without storage etchings");
		for (int i = 0; i < 4; i++) {
			be.etchings().setStackInSlot(i, Support.etching(EtchingType.STORAGE, EtchingTier.T4, 1));
		}
		Support.expectEquals(helper, 8, be.upgrades().bufferSlots(), "buffer slots");
		Support.expectEquals(helper, 99, be.display().getSlotLimit(0), "limit with four storage_4 etchings");
		be.display().insertItem(0, new ItemStack(Items.COBBLESTONE, 64), false);
		ItemStack left = be.display().insertItem(0, new ItemStack(Items.COBBLESTONE, 64), false);
		Support.expectEquals(helper, 29, left.getCount(), "items that did not fit");
		Support.expectEquals(helper, 99, be.getDisplayedItem().getCount(), "buffered items");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void fullBufferSavesAndReloads(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		for (int i = 0; i < 4; i++) {
			be.etchings().setStackInSlot(i, Support.etching(EtchingType.STORAGE, EtchingTier.T4, 1));
		}
		be.display().insertItem(0, new ItemStack(Items.COBBLESTONE, 64), false);
		be.display().insertItem(0, new ItemStack(Items.COBBLESTONE, 64), false);
		PlinthBlockEntity loaded = Support.reload(helper, be, POS);
		Support.expectStack(helper, new ItemStack(Items.COBBLESTONE, 99), loaded.getDisplayedItem(), "after nbt reload");
		Support.expectHandler(helper, be.etchings(), loaded.etchings(), "etchings after nbt reload");
		PlinthBlockEntity client = Support.viaPacket(helper, be, POS);
		Support.expectStack(helper, new ItemStack(Items.COBBLESTONE, 99), client.getDisplayedItem(), "after update packet");
		helper.succeed();
	}
}
