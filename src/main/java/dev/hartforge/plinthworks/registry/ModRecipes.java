package dev.hartforge.plinthworks.registry;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.recipe.*;
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
