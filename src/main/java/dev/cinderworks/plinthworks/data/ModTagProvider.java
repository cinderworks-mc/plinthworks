package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.registry.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.*;

import java.util.concurrent.CompletableFuture;

public class ModTagProvider extends BlockTagsProvider
{
	public ModTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
			ExistingFileHelper files) {
		super(output, registries, Plinthworks.MODID, files);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		tag(ModTags.PLINTH_BASES).add(
				Blocks.STONE, Blocks.COBBLESTONE, Blocks.SMOOTH_STONE, Blocks.STONE_BRICKS,
				Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
				Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS,
				Blocks.BRICKS, Blocks.MUD_BRICKS, Blocks.SANDSTONE, Blocks.RED_SANDSTONE,
				Blocks.QUARTZ_BLOCK, Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS,
				Blocks.NETHER_BRICKS, Blocks.END_STONE, Blocks.PURPUR_BLOCK);
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PLINTH.get());
	}
}
