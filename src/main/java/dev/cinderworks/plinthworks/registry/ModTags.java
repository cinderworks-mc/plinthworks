package dev.cinderworks.plinthworks.registry;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags
{
	public static final TagKey<Block> PLINTH_BASES = TagKey.create(Registries.BLOCK,
			Plinthworks.id("plinth_bases"));
}
