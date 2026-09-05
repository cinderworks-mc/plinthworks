package dev.hartforge.plinthworks.menu;

import dev.hartforge.plinthworks.item.EtchingItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EtchingSlot extends SlotItemHandler
{
	public EtchingSlot(IItemHandler handler, int index, int x, int y) {
		super(handler, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof EtchingItem && super.mayPlace(stack);
	}
}
