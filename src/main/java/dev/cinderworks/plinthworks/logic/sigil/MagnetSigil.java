package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class MagnetSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		if (be.resourceMode() != ResourceMode.ITEM || !(be.getLevel() instanceof ServerLevel level)) {
			return;
		}
		List<IItemHandler> dests = WorldSigilSupport.handlers(be, upgrades);
		double x = be.getBlockPos().getX() + 0.5;
		double y = be.getBlockPos().getY() + 0.5;
		double z = be.getBlockPos().getZ() + 0.5;
		AABB box = new AABB(x - upgrades.range(), y - upgrades.range(), z - upgrades.range(),
				x + upgrades.range(), y + upgrades.range(), z + upgrades.range());
		int left = upgrades.throughput();
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box)) {
			ItemStack stack = entity.getItem();
			if (left <= 0 || stack.isEmpty() || !WorldSigilSupport.filterAllows(be, upgrades, stack)) {
				continue;
			}
			int moved = WorldSigilSupport.insert(dests, stack, left);
			left -= moved;
			if (stack.isEmpty()) {
				entity.discard();
			} else if (moved > 0) {
				entity.setItem(stack);
			}
		}
	}
}
