package dev.hartforge.plinthworks.data;

import dev.hartforge.plinthworks.registry.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ModLootProvider extends LootTableProvider
{
	public ModLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), registries);
	}

	private static class BlockLoot extends BlockLootSubProvider
	{
		protected BlockLoot(HolderLookup.Provider registries) {
			super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
		}

		@Override
		protected void generate() {
			Block plinth = ModBlocks.PLINTH.get();
			add(plinth, LootTable.lootTable().withPool(applyExplosionCondition(plinth, LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(ModItems.PLINTH.get())
							.apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
									.include(ModDataComponents.PLINTH_CONTENTS.get()))))));
		}

		@Override
		protected Iterable<Block> getKnownBlocks() {
			return List.of(ModBlocks.PLINTH.get());
		}
	}
}
