package dev.hartforge.plinthworks.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class PlinthRecipeSerializer implements RecipeSerializer<PlinthCraftRecipe>
{
	private static final MapCodec<PlinthCraftRecipe> CODEC = MapCodec.unit(PlinthCraftRecipe.INSTANCE);
	private static final StreamCodec<RegistryFriendlyByteBuf, PlinthCraftRecipe> STREAM_CODEC =
			StreamCodec.unit(PlinthCraftRecipe.INSTANCE);

	@Override
	public MapCodec<PlinthCraftRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PlinthCraftRecipe> streamCodec() {
		return STREAM_CODEC;
	}
}
