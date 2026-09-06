package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.recipe.PlinthCraftRecipe;
import dev.cinderworks.plinthworks.registry.ModItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.*;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider
{
	public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput output) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PLINTH_CORE.get())
				.pattern(" I ").pattern("SSS")
				.define('I', Items.IRON_INGOT).define('S', Items.STONE_SLAB)
				.unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output);

		blank(output, ModItems.SIGIL_BASE.get(), Items.COPPER_INGOT, Items.GOLD_NUGGET);
		blank(output, ModItems.ETCHING_BASE.get(), Items.IRON_NUGGET, Items.REDSTONE);
		blank(output, ModItems.SEAL_BASE.get(), Items.PAPER, Items.GOLD_NUGGET);
		sigil(output, SigilType.IMPORT, Items.HOPPER);
		sigil(output, SigilType.EXPORT, Items.DROPPER);
		sigil(output, SigilType.CRAFTING, Items.CRAFTING_TABLE);
		sigil(output, SigilType.VOID, Items.OBSIDIAN);
		seal(output, SealType.ITEM, Items.PAPER);
		seal(output, SealType.ITEM_EXACT, Items.COMPARATOR);
		seal(output, SealType.MOD, Items.BOOKSHELF);
		seal(output, SealType.TAG, Items.NAME_TAG);
		seal(output, SealType.COMPONENT, Items.AMETHYST_SHARD);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LINK_TOOL.get())
				.pattern(" C ").pattern(" RS").pattern("S  ")
				.define('C', Items.COPPER_INGOT).define('R', Items.REDSTONE).define('S', Items.STICK)
				.unlockedBy("has_redstone", has(Items.REDSTONE)).save(output);

		etchingLadder(output, EtchingType.SPEED, Items.REDSTONE);
		etchingLadder(output, EtchingType.CAPACITY, Items.CHEST);
		etchingLadder(output, EtchingType.STORAGE, Items.BARREL);
		etchingLadder(output, EtchingType.RANGE, Items.ENDER_PEARL);

		ResourceLocation plinthId = Plinthworks.id("plinth");
		Advancement.Builder advancement = output.advancement()
				.addCriterion("has_plinth_core", has(ModItems.PLINTH_CORE.get()))
				.rewards(AdvancementRewards.Builder.recipe(plinthId))
				.requirements(AdvancementRequirements.Strategy.OR);
		output.accept(plinthId, new PlinthCraftRecipe(), advancement.build(plinthId.withPrefix("recipes/misc/")));
	}

	private static void blank(RecipeOutput output, Item result, Item first, Item second) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
				.requires(first).requires(second).unlockedBy("has_material", has(first)).save(output);
	}

	private static void sigil(RecipeOutput output, SigilType type, Item ingredient) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SIGILS.get(type).get())
				.requires(ModItems.SIGIL_BASE.get()).requires(ingredient)
				.unlockedBy("has_sigil_base",
						InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SIGIL_BASE.get())).save(output);
	}

	private static void seal(RecipeOutput output, SealType type, Item ingredient) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SEALS.get(type).get())
				.requires(ModItems.SEAL_BASE.get()).requires(ingredient)
				.unlockedBy("has_seal_base",
						InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SEAL_BASE.get())).save(output);
	}

	private static void etchingLadder(RecipeOutput output, EtchingType type, Item ingredient) {
		for (EtchingTier tier : EtchingTier.values()) {
			ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,
					ModItems.ETCHINGS.get(type).get(tier).get()).requires(ingredient);
			if (tier == EtchingTier.T1) {
				builder.requires(ModItems.ETCHING_BASE.get());
			} else {
				builder.requires(ModItems.ETCHINGS.get(type).get(EtchingTier.values()[tier.ordinal() - 1]).get());
			}
			builder.unlockedBy("has_etching_base",
					InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ETCHING_BASE.get())).save(output);
		}
	}
}
