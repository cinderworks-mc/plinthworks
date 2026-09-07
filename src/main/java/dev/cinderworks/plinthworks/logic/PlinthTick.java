package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.resource.*;
import dev.cinderworks.plinthworks.logic.sigil.SigilBehavior;
import dev.cinderworks.plinthworks.logic.sigil.SigilBehaviors;
import dev.cinderworks.plinthworks.logic.network.PlinthNetwork;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;
import java.util.function.Predicate;

public class PlinthTick
{
	public static void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		SigilBehavior behavior = SigilBehaviors.get(upgrades.verb());
		if (behavior != null) {
			behavior.run(be, upgrades);
		}
	}

	// import only sources from the plinth's own nearby blocks; the network is fed by export.
	public static void pull(PlinthBlockEntity be, UpgradeSet upgrades) {
		pull(be, upgrades, ResourceTypes.get(be.resourceMode()));
	}

	private static <T, R> void pull(PlinthBlockEntity be, UpgradeSet upgrades, ResourceType<T, R> type) {
		int left = type.throughput(upgrades);
		T dest = type.buffer(be);
		Predicate<R> filter = type.filter(be, upgrades);
		for (T source : type.nearby(be, upgrades.range())) {
			left -= type.move(source, dest, left, filter);
			if (left <= 0) {
				return;
			}
		}
	}

	// export also drains linked plinths, so an import/export pair shifts resources across a channel
	public static void push(PlinthBlockEntity be, UpgradeSet upgrades) {
		push(be, upgrades, ResourceTypes.get(be.resourceMode()));
	}

	private static <T, R> void push(PlinthBlockEntity be, UpgradeSet upgrades, ResourceType<T, R> type) {
		List<T> dests = type.nearby(be, upgrades.range());
		if (dests.isEmpty()) {
			return;
		}
		List<T> sources = new ArrayList<>();
		sources.add(type.buffer(be));
		sources.addAll(networkPlinths(be, type));
		Predicate<R> filter = type.filter(be, upgrades);
		int left = type.throughput(upgrades);
		for (T source : sources) {
			for (T dest : dests) {
				left -= type.move(source, dest, left, filter);
				if (left <= 0) {
					return;
				}
			}
		}
	}

	private static <T, R> List<T> networkPlinths(PlinthBlockEntity be, ResourceType<T, R> type) {
		Level level = be.getLevel();
		ArrayList<T> handlers = new ArrayList<>();
		if (be.channel().isEmpty() || !(level instanceof ServerLevel serverLevel)) {
			return handlers;
		}
		for (net.minecraft.core.BlockPos pos : PlinthNetwork.get(serverLevel)
				.orderedMembers(be.channel(), be.getBlockPos(), serverLevel, be.resourceMode())) {
			if (!level.hasChunkAt(pos)) {
				continue;
			}
			T plinth = type.find(level, pos, null);
			if (plinth != null) {
				handlers.add(plinth);
			}
		}
		return handlers;
	}

	public static boolean craft(PlinthBlockEntity be, UpgradeSet upgrades) {
		if (be.resourceMode() != ResourceMode.ITEM) {
			return false;
		}
		Level level = be.getLevel();
		ItemStack target = be.getDisplayedItem();
		if (target.isEmpty()) {
			return false;
		}
		HolderLookup.Provider registries = level.registryAccess();
		List<IItemHandler> handlers = targetHandlers(be, upgrades, ResourceTypes.ITEM);
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
		voidBuffer(be, upgrades, ResourceTypes.get(be.resourceMode()));
	}

	private static <T, R> void voidBuffer(PlinthBlockEntity be, UpgradeSet upgrades, ResourceType<T, R> type) {
		int left = type.throughput(upgrades);
		Predicate<R> filter = type.filter(be, upgrades);
		left -= type.discard(type.buffer(be), left, filter);
		for (T source : type.nearby(be, upgrades.range())) {
			left -= type.discard(source, left, filter);
			if (left <= 0) {
				return;
			}
		}
	}

	public static List<IItemHandler> itemHandlers(PlinthBlockEntity be, UpgradeSet upgrades) {
		return targetHandlers(be, upgrades, ResourceTypes.ITEM);
	}

	private static <T, R> List<T> targetHandlers(PlinthBlockEntity be, UpgradeSet upgrades,
			ResourceType<T, R> type) {
		Level level = be.getLevel();
		ArrayList<T> handlers = new ArrayList<>();
		if (!be.channel().isEmpty() && level instanceof ServerLevel serverLevel) {
			for (net.minecraft.core.BlockPos pos : PlinthNetwork.get(serverLevel)
					.orderedMembers(be.channel(), be.getBlockPos(), serverLevel, be.resourceMode())) {
				if (!level.hasChunkAt(pos)) {
					continue;
				}
				T plinth = type.find(level, pos, null);
				if (plinth != null) {
					handlers.add(plinth);
				}
				handlers.addAll(type.adjacent(level, pos));
			}
		}
		handlers.addAll(type.nearby(be, upgrades.range()));
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
