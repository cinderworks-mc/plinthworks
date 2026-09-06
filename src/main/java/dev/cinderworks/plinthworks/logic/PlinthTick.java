package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.sigil.SigilBehavior;
import dev.cinderworks.plinthworks.logic.sigil.SigilBehaviors;
import dev.cinderworks.plinthworks.logic.network.PlinthNetwork;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;

public class PlinthTick
{
	public static void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		SigilBehavior behavior = SigilBehaviors.get(upgrades.verb());
		if (behavior != null) {
			behavior.run(be, upgrades);
		}
	}

	// import only sources from the plinth's own adjacent inventories; the network is fed by export.
	public static void pull(PlinthBlockEntity be, UpgradeSet upgrades) {
		int moved = 0;
		for (IItemHandler handler : InventoryScan.nearbyHandlers(be, upgrades.range())) {
			for (int slot = 0; slot < handler.getSlots() && moved < upgrades.throughput(); slot++) {
				ItemStack found = handler.extractItem(slot, upgrades.throughput() - moved, true);
				if (found.isEmpty() || upgrades.filtered() && !be.matchesFilter(found)) {
					continue;
				}
				ItemStack remainder = be.display().insertItem(0, found, true);
				int accepted = TransferMath.moveCount(found.getCount(),
						found.getCount() - remainder.getCount(), upgrades.throughput() - moved);
				if (accepted == 0) {
					continue;
				}
				ItemStack extracted = handler.extractItem(slot, accepted, false);
				be.display().insertItem(0, extracted, false);
				moved += extracted.getCount();
			}
			if (moved >= upgrades.throughput()) {
				return;
			}
		}
	}

	// export also drains linked plinths, so an import/export pair shifts items across a channel
	public static void push(PlinthBlockEntity be, UpgradeSet upgrades) {
		List<IItemHandler> dests = InventoryScan.nearbyHandlers(be, upgrades.range());
		if (dests.isEmpty()) {
			return;
		}
		List<IItemHandler> sources = new ArrayList<>();
		sources.add(be.display());
		sources.addAll(networkPlinths(be));
		int left = upgrades.throughput();
		for (IItemHandler source : sources) {
			for (int slot = 0; slot < source.getSlots() && left > 0; slot++) {
				ItemStack shown = source.extractItem(slot, left, true);
				if (shown.isEmpty() || upgrades.filtered() && !be.matchesFilter(shown)) {
					continue;
				}
				left -= deposit(source, slot, shown, dests, left);
			}
			if (left <= 0) {
				return;
			}
		}
	}

	private static int deposit(IItemHandler source, int slot, ItemStack shown, List<IItemHandler> dests, int budget) {
		int moved = 0;
		for (IItemHandler dest : dests) {
			for (int d = 0; d < dest.getSlots() && moved < budget; d++) {
				ItemStack offered = shown.copyWithCount(budget - moved);
				ItemStack remainder = dest.insertItem(d, offered, true);
				int accepted = TransferMath.moveCount(offered.getCount(),
						offered.getCount() - remainder.getCount(), budget - moved);
				if (accepted == 0) {
					continue;
				}
				ItemStack extracted = source.extractItem(slot, accepted, false);
				if (extracted.isEmpty()) {
					continue;
				}
				ItemStack failed = dest.insertItem(d, extracted, false);
				if (!failed.isEmpty()) {
					source.insertItem(slot, failed, false);
				}
				moved += extracted.getCount() - failed.getCount();
			}
		}
		return moved;
	}

	private static List<IItemHandler> networkPlinths(PlinthBlockEntity be) {
		Level level = be.getLevel();
		ArrayList<IItemHandler> handlers = new ArrayList<>();
		if (be.channel().isEmpty() || !(level instanceof ServerLevel serverLevel)) {
			return handlers;
		}
		for (net.minecraft.core.BlockPos pos : PlinthNetwork.get(serverLevel)
				.orderedMembers(be.channel(), be.getBlockPos(), serverLevel)) {
			if (!level.hasChunkAt(pos)) {
				continue;
			}
			IItemHandler plinth = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (plinth != null) {
				handlers.add(plinth);
			}
		}
		return handlers;
	}

	public static boolean craft(PlinthBlockEntity be, UpgradeSet upgrades) {
		Level level = be.getLevel();
		ItemStack target = be.getDisplayedItem();
		if (target.isEmpty()) {
			return false;
		}
		HolderLookup.Provider registries = level.registryAccess();
		List<IItemHandler> handlers = targetHandlers(be, upgrades);
		for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
			CraftingRecipe recipe = holder.value();
			ItemStack result = recipe.getResultItem(registries);
			if (!ItemStack.isSameItem(result, target)) {
				continue;
			}
			List<Source> sources = findIngredients(recipe.getIngredients(), handlers);
			if (sources.isEmpty()) {
				continue;
			}
			if (!ItemStack.isSameItemSameComponents(result, target)) {
				continue;
			}
			if (!be.display().insertItem(0, result.copy(), true).isEmpty()) {
				continue;
			}
			for (Source source : sources) {
				source.handler.extractItem(source.slot, 1, false);
			}
			// TODO: return crafting remainders and handle component-sensitive tag ingredients
			be.display().insertItem(0, result.copy(), false);
			return true;
		}
		return false;
	}

	public static void voidBuffer(PlinthBlockEntity be, UpgradeSet upgrades) {
		int budget = upgrades.throughput();
		ItemStack shown = be.getDisplayedItem();
		if (!shown.isEmpty() && (!upgrades.filtered() || be.matchesFilter(shown))) {
			budget -= be.display().extractItem(0, budget, false).getCount();
		}
		for (IItemHandler handler : InventoryScan.nearbyHandlers(be, upgrades.range())) {
			for (int slot = 0; slot < handler.getSlots() && budget > 0; slot++) {
				ItemStack found = handler.extractItem(slot, budget, true);
				if (found.isEmpty() || upgrades.filtered() && !be.matchesFilter(found)) {
					continue;
				}
				budget -= handler.extractItem(slot, budget, false).getCount();
			}
			if (budget <= 0) {
				return;
			}
		}
	}

	private static List<IItemHandler> targetHandlers(PlinthBlockEntity be, UpgradeSet upgrades) {
		Level level = be.getLevel();
		ArrayList<IItemHandler> handlers = new ArrayList<>();
		if (!be.channel().isEmpty() && level instanceof ServerLevel serverLevel) {
			for (net.minecraft.core.BlockPos pos : PlinthNetwork.get(serverLevel)
					.orderedMembers(be.channel(), be.getBlockPos(), serverLevel)) {
				if (!level.hasChunkAt(pos)) {
					continue;
				}
				IItemHandler plinth = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
				if (plinth != null) {
					handlers.add(plinth);
				}
				handlers.addAll(InventoryScan.adjacentHandlers(level, pos));
			}
		}
		handlers.addAll(InventoryScan.nearbyHandlers(be, upgrades.range()));
		return handlers;
	}

	private static List<Source> findIngredients(List<Ingredient> ingredients, List<IItemHandler> handlers) {
		List<Source> found = new ArrayList<>();
		Map<Slot, Integer> reserved = new HashMap<>();
		for (Ingredient ingredient : ingredients) {
			if (ingredient.isEmpty()) {
				continue;
			}
			Source match = null;
			for (IItemHandler handler : handlers) {
				for (int slot = 0; slot < handler.getSlots(); slot++) {
					ItemStack stack = handler.getStackInSlot(slot);
					Slot key = new Slot(handler, slot);
					if (ingredient.test(stack) && stack.getCount() > reserved.getOrDefault(key, 0)) {
						match = new Source(handler, slot);
						reserved.merge(key, 1, Integer::sum);
						break;
					}
				}
				if (match != null) {
					break;
				}
			}
			if (match == null) {
				return List.of();
			}
			found.add(match);
		}
		return found;
	}

	private record Source(IItemHandler handler, int slot) {}

	private record Slot(IItemHandler handler, int slot) {}
}
