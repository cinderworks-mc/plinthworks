package dev.hartforge.plinthworks.registry;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.block.PlinthBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks
{
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Plinthworks.MODID);

	public static final DeferredBlock<PlinthBlock> PLINTH = BLOCKS.register("plinth",
			() -> new PlinthBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE).strength(1.5F, 6.0F).sound(SoundType.STONE)
					.requiresCorrectToolForDrops().noOcclusion()));
}
