package dev.cinderworks.plinthworks.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

// holds a count-1 representative of a filter key, never a real item - SealMenu.clicked drives it
public class GhostSlot extends SlotItemHandler
{
	public GhostSlot(IItemHandler handler, int index, int x, int y) {
		super(handler, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}
}
