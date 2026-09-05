package dev.hartforge.plinthworks.data;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider
{
	public ModBlockStateProvider(PackOutput output, ExistingFileHelper files) {
		super(output, Plinthworks.MODID, files);
	}

	@Override
	protected void registerStatesAndModels() {
		ModelFile plinth = models().getBuilder("plinth")
				.texture("particle", mcLoc("block/smooth_stone"))
				.texture("body", mcLoc("block/smooth_stone"))
				.element().from(2, 0, 2).to(14, 12, 14)
				.allFaces((dir, face) -> face.texture("#body")).end();
		simpleBlock(ModBlocks.PLINTH.get(), plinth);
	}
}
