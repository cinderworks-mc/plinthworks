package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class ShiftClickGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void shiftClickIntoEmptySlotsKeepsEverything(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		ServerPlayer player = Support.player(helper);
		Inventory inventory = player.getInventory();
		inventory.setItem(0, Support.sigil(SigilType.EXPORT));
		inventory.setItem(1, Support.etching(EtchingType.RANGE, EtchingTier.T2, 1));
		inventory.setItem(2, Support.seal(SealType.MOD, true));
		inventory.setItem(3, new ItemStack(Items.COBBLESTONE, 12));
		PlinthMenu menu = new PlinthMenu(1, inventory, be);
		for (int i = 0; i < 4; i++) {
			menu.quickMoveStack(player, Support.menuSlot(menu, inventory, i));
		}
		Support.expectEquals(helper, SigilType.EXPORT, be.upgrades().verb(), "sigil after shift click");
		Support.expectEquals(helper, 2, be.upgrades().range() - 1, "range levels after shift click");
		Support.expectStack(helper, Support.seal(SealType.MOD, true), be.seals().getStackInSlot(0), "seal slot");
		Support.expectStack(helper, new ItemStack(Items.COBBLESTONE, 12), be.getDisplayedItem(), "display slot");
		for (int i = 0; i < 4; i++) {
			Support.expect(helper, inventory.getItem(i).isEmpty(), "inventory slot " + i + " still holds " + inventory.getItem(i));
		}
		PlinthBlockEntity loaded = Support.reload(helper, be, POS);
		Support.expectHandler(helper, be.sigil(), loaded.sigil(), "sigil after reload");
		Support.expectHandler(helper, be.etchings(), loaded.etchings(), "etchings after reload");
		Support.expectHandler(helper, be.seals(), loaded.seals(), "seals after reload");
		Support.expectHandler(helper, be.display(), loaded.display(), "display after reload");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void shiftClickMergeIntoEtchingMarksPlinthChanged(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		ServerPlayer player = Support.player(helper);
		Inventory inventory = player.getInventory();
		be.etchings().setStackInSlot(0, Support.etching(EtchingType.STORAGE, EtchingTier.T1, 1));
		Support.expectEquals(helper, 1, be.upgrades().storageLevel(), "storage level before");
		inventory.setItem(0, Support.etching(EtchingType.STORAGE, EtchingTier.T1, 1));
		PlinthMenu menu = new PlinthMenu(1, inventory, be);
		LevelChunk chunk = helper.getLevel().getChunkAt(helper.absolutePos(POS));
		chunk.setUnsaved(false);
		menu.quickMoveStack(player, Support.menuSlot(menu, inventory, 0));
		Support.expect(helper, chunk.isUnsaved(), "chunk was not marked unsaved after the merge");
		Support.expectEquals(helper, 2, be.etchings().getStackInSlot(0).getCount(), "merged etchings");
		Support.expectEquals(helper, 2, be.upgrades().storageLevel(), "storage level after");
		Support.expect(helper, inventory.getItem(0).isEmpty(), "etching stayed in the inventory");
		PlinthBlockEntity loaded = Support.reload(helper, be, POS);
		Support.expectEquals(helper, 2, loaded.etchings().getStackInSlot(0).getCount(), "etchings after reload");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void shiftClickMergeIntoDisplayMarksPlinthChanged(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		ServerPlayer player = Support.player(helper);
		Inventory inventory = player.getInventory();
		be.display().setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 3));
		inventory.setItem(0, new ItemStack(Items.COBBLESTONE, 5));
		PlinthMenu menu = new PlinthMenu(1, inventory, be);
		LevelChunk chunk = helper.getLevel().getChunkAt(helper.absolutePos(POS));
		chunk.setUnsaved(false);
		menu.quickMoveStack(player, Support.menuSlot(menu, inventory, 0));
		Support.expect(helper, chunk.isUnsaved(), "chunk was not marked unsaved after the merge");
		Support.expectEquals(helper, 8, be.getDisplayedItem().getCount(), "merged items");
		Support.expect(helper, inventory.getItem(0).isEmpty(), "items stayed in the inventory");
		Support.expectEquals(helper, 8, Support.reload(helper, be, POS).getDisplayedItem().getCount(), "items after reload");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void shiftClickOutOfSlotsLeavesNothingBehind(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		ServerPlayer player = Support.player(helper);
		Inventory inventory = player.getInventory();
		be.sigil().setStackInSlot(0, Support.sigil(SigilType.IMPORT));
		be.etchings().setStackInSlot(0, Support.etching(EtchingType.SPEED, EtchingTier.T3, 1));
		be.display().setStackInSlot(0, new ItemStack(Items.DIRT, 20));
		Support.expectEquals(helper, SigilType.IMPORT, be.upgrades().verb(), "sigil before");
		PlinthMenu menu = new PlinthMenu(1, inventory, be);
		// menu order is sigil, four etchings, three seals, display
		menu.quickMoveStack(player, 0);
		menu.quickMoveStack(player, 1);
		menu.quickMoveStack(player, 8);
		Support.expect(helper, be.upgrades().verb() == null, "plinth still runs " + be.upgrades().verb() + " after the sigil left");
		Support.expectEquals(helper, 1, inventory.countItem(ModItems.SIGILS.get(SigilType.IMPORT).get()), "sigils in the inventory");
		Support.expectEquals(helper, 1, inventory.countItem(ModItems.ETCHINGS.get(EtchingType.SPEED).get(EtchingTier.T3).get()),
				"etchings in the inventory");
		Support.expectEquals(helper, 20, inventory.countItem(Items.DIRT), "dirt in the inventory");
		Support.expect(helper, be.sigil().getStackInSlot(0).isEmpty() && be.etchings().getStackInSlot(0).isEmpty()
				&& be.getDisplayedItem().isEmpty(), "plinth still holds items");
		helper.succeed();
	}
}
