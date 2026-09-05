package dev.cinderworks.plinthworks.menu;

import dev.cinderworks.plinthworks.item.SealItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SealSlot extends SlotItemHandler
{
	public SealSlot(IItemHandler handler, int index, int x, int y) {
		super(handler, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof SealItem && super.mayPlace(stack);
	}
}
