package dev.cinderworks.plinthworks.menu;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.item.SigilItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SigilSlot extends SlotItemHandler
{
	private final PlinthBlockEntity be;

	public SigilSlot(PlinthBlockEntity be, IItemHandler handler, int index, int x, int y) {
		super(handler, index, x, y);
		this.be = be;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof SigilItem && super.mayPlace(stack);
	}

	// the inherited container is empty, so tell the plinth ourselves
	@Override
	public void setChanged() {
		be.slotsChanged();
	}
}
