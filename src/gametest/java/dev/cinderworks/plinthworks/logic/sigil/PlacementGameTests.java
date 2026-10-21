package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.logic.PlinthFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@PrefixGameTestTemplate(false)
public class PlacementGameTests
{
	@GameTest(template = "empty", templateNamespace = "plinthworks")
	public static void refusedExtractionDoesNotPlaceBlock(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		ItemStackHandler source = new ItemStackHandler(1) {
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return simulate ? super.extractItem(slot, amount, true) : ItemStack.EMPTY;
			}
		};
		source.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
		ItemStack shown = source.extractItem(0, 1, true);
		boolean placed = WorldSigilSupport.place(helper.getLevel(),
				PlinthFakePlayer.get(helper.getLevel(), helper.absolutePos(pos)),
				new WorldSigilSupport.Source(source, 0, shown), helper.absolutePos(pos), Direction.UP);
		if (placed || !helper.getBlockState(pos).isAir() || source.getStackInSlot(0).getCount() != 1) {
			helper.fail("a refused extraction placed a block or consumed its item", pos);
		}
		helper.succeed();
	}
}
