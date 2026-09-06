package dev.cinderworks.plinthworks.recipe;

import dev.cinderworks.plinthworks.component.PlinthContents;
import dev.cinderworks.plinthworks.registry.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class PlinthCraftRecipe implements CraftingRecipe
{
	// stateless, so one shared instance. the codecs hand this back on decode so
	// StreamCodec.unit's encode-time equality check passes when syncing to clients.
	public static final PlinthCraftRecipe INSTANCE = new PlinthCraftRecipe();

	@Override
	public boolean matches(CraftingInput input, Level level) {
		Block base = null;
		int bases = 0;
		int cores = 0;
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.is(ModItems.PLINTH_CORE.get())) {
				cores++;
				continue;
			}
			if (!(stack.getItem() instanceof BlockItem blockItem)
					|| !blockItem.getBlock().builtInRegistryHolder().is(ModTags.PLINTH_BASES)) {
				return false;
			}
			if (base != null && base != blockItem.getBlock()) {
				return false;
			}
			base = blockItem.getBlock();
			bases++;
		}
		return cores == 1 && bases == 4;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.getItem() instanceof BlockItem blockItem
					&& blockItem.getBlock().builtInRegistryHolder().is(ModTags.PLINTH_BASES)) {
				ItemStack plinth = new ItemStack(ModItems.PLINTH.get());
				plinth.set(ModDataComponents.PLINTH_CONTENTS.get(),
						new PlinthContents(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock())));
				return plinth;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 5;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return new ItemStack(ModItems.PLINTH.get());
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.PLINTH.get();
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}
}
