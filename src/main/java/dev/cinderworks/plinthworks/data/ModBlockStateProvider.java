package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.registry.ModBlocks;
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
		// mirrors the BER: base, chamfer, stem, cap, plus a bronze fitting on the north cap face
		ModelFile plinth = models().getBuilder("plinth")
				.texture("particle", mcLoc("block/smooth_stone"))
				.texture("body", mcLoc("block/smooth_stone"))
				.texture("fitting", modLoc("block/plinth_fitting"))
				.element().from(3, 0, 3).to(13, 3, 13)
				.allFaces((dir, face) -> face.texture("#body")).end()
				.element().from(4, 3, 4).to(12, 4, 12)
				.allFaces((dir, face) -> face.texture("#body")).end()
				.element().from(5, 4, 5).to(11, 10, 11)
				.allFaces((dir, face) -> face.texture("#body")).end()
				.element().from(3, 10, 3).to(13, 14, 13)
				.allFaces((dir, face) -> face.texture("#body")).end()
				.element().from(5, 11, 2.5F).to(11, 13, 3.0F)
				.allFaces((dir, face) -> face.texture("#fitting")).end();
		simpleBlock(ModBlocks.PLINTH.get(), plinth);
	}
}
