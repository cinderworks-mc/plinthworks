package dev.cinderworks.plinthworks.logic.resource;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.*;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.Predicate;

public class ItemResourceType implements ResourceType<IItemHandler, ItemStack>
{
	@Override
	public IItemHandler find(Level level, BlockPos pos, Direction side) {
		return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
	}

	@Override
	public IItemHandler buffer(PlinthBlockEntity be) {
		return be.display();
	}

	@Override
	public Predicate<ItemStack> filter(PlinthBlockEntity be, UpgradeSet upgrades) {
		return upgrades.filtered() ? be::matchesFilter : stack -> true;
	}

	@Override
	public int move(IItemHandler source, IItemHandler dest, int amount, Predicate<ItemStack> filter) {
		int moved = 0;
		for (int slot = 0; slot < source.getSlots() && moved < amount; slot++) {
			ItemStack shown = source.extractItem(slot, amount - moved, true);
			if (shown.isEmpty() || !filter.test(shown)) {
				continue;
			}
			for (int d = 0; d < dest.getSlots() && moved < amount; d++) {
				ItemStack offered = shown.copyWithCount(amount - moved);
				ItemStack remainder = dest.insertItem(d, offered, true);
				int accepted = TransferMath.moveCount(offered.getCount(),
						offered.getCount() - remainder.getCount(), amount - moved);
				if (accepted == 0) {
					continue;
				}
				ItemStack extracted = source.extractItem(slot, accepted, false);
				ItemStack failed = dest.insertItem(d, extracted, false);
				if (!failed.isEmpty()) {
					source.insertItem(slot, failed, false);
				}
				moved += extracted.getCount() - failed.getCount();
				shown.shrink(extracted.getCount());
			}
		}
		return moved;
	}

	@Override
	public int discard(IItemHandler source, int amount, Predicate<ItemStack> filter) {
		int moved = 0;
		for (int slot = 0; slot < source.getSlots() && moved < amount; slot++) {
			ItemStack shown = source.extractItem(slot, amount - moved, true);
			if (!shown.isEmpty() && filter.test(shown)) {
				moved += source.extractItem(slot, amount - moved, false).getCount();
			}
		}
		return moved;
	}

	@Override
	public int throughput(UpgradeSet upgrades) {
		return upgrades.throughput();
	}
}
