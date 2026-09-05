package dev.cinderworks.plinthworks.registry;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.recipe.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes
{
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
			DeferredRegister.create(Registries.RECIPE_SERIALIZER, Plinthworks.MODID);

	public static final Supplier<RecipeSerializer<PlinthCraftRecipe>> PLINTH =
			SERIALIZERS.register("plinth", PlinthRecipeSerializer::new);
}
