package dev.hartforge.plinthworks.menu;

import dev.hartforge.plinthworks.item.SigilItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SigilSlot extends SlotItemHandler
{
	public SigilSlot(IItemHandler handler, int index, int x, int y) {
		super(handler, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof SigilItem && super.mayPlace(stack);
	}
}
