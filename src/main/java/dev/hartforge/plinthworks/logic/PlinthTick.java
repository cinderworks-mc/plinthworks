package dev.hartforge.plinthworks.logic;

import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.logic.sigil.SigilBehavior;
import dev.hartforge.plinthworks.logic.sigil.SigilBehaviors;
import dev.hartforge.plinthworks.logic.network.PlinthNetwork;
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

	public static void pull(PlinthBlockEntity be, UpgradeSet upgrades) {
		int moved = 0;
		for (IItemHandler handler : targetHandlers(be, upgrades)) {
			for (int slot = 0; slot < handler.getSlots() && moved < upgrades.throughput(); slot++) {
				ItemStack found = handler.extractItem(slot, upgrades.throughput() - moved, true);
				if (found.isEmpty() || upgrades.filtered() && !be.matchesFilter(found)) {
					continue;
				}
				ItemStack remainder = be.display().insertItem(0, found, true);
				int accepted = found.getCount() - remainder.getCount();
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

	public static void push(PlinthBlockEntity be, UpgradeSet upgrades) {
		ItemStack shown = be.getDisplayedItem();
		if (shown.isEmpty() || upgrades.filtered() && !be.matchesFilter(shown)) {
			return;
		}

		int left = Math.min(shown.getCount(), upgrades.throughput());
		for (IItemHandler handler : targetHandlers(be, upgrades)) {
			for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
				ItemStack offered = shown.copyWithCount(left);
				ItemStack remainder = handler.insertItem(slot, offered, true);
				int accepted = offered.getCount() - remainder.getCount();
				if (accepted == 0) {
					continue;
				}
				ItemStack extracted = be.display().extractItem(0, accepted, false);
				ItemStack failed = handler.insertItem(slot, extracted, false);
				if (!failed.isEmpty()) {
					be.display().insertItem(0, failed, false);
				}
				left -= accepted - failed.getCount();
			}
			if (left == 0) {
				return;
			}
		}
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
			int room = target.getMaxStackSize() * upgrades.bufferSlots() - target.getCount();
			if (!ItemStack.isSameItemSameComponents(result, target) || result.getCount() > room) {
				return false;
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
		be.display().extractItem(0, upgrades.throughput(), false);
	}

	private static List<IItemHandler> targetHandlers(PlinthBlockEntity be, UpgradeSet upgrades) {
		Level level = be.getLevel();
		ArrayList<IItemHandler> handlers = new ArrayList<>();
		if (!be.channel().isEmpty() && level instanceof ServerLevel serverLevel) {
			for (net.minecraft.core.BlockPos pos : PlinthNetwork.get(serverLevel)
					.orderedMembers(be.channel(), be.getBlockPos(), serverLevel)) {
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
