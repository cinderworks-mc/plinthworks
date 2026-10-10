package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.block.PlinthBlock;
import dev.cinderworks.plinthworks.logic.PlinthVisual;
import dev.cinderworks.plinthworks.logic.ResourceMode;
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
		// the worn block body is drawn by the block entity renderer, these parts are the trim around it
		ModelFile fixed = PlinthModels.fixed(models().getBuilder("plinth_fixed"));
		ModelFile worn = PlinthModels.worn(models().getBuilder("plinth_worn"));
		ModelFile glow = PlinthModels.glow(models().getBuilder("plinth_glow"));

		MultiPartBlockStateBuilder state = getMultipartBuilder(ModBlocks.PLINTH.get());
		state.part().modelFile(fixed).addModel().end();
		state.part().modelFile(worn).addModel().end();
		state.part().modelFile(glow).addModel().condition(PlinthBlock.ACTIVE, true).end();
		for (ResourceMode mode : ResourceMode.values()) {
			ModelFile glyph = PlinthModels.glyph(models().getBuilder("plinth_glyph_" + mode.name().toLowerCase()), mode.ordinal());
			state.part().modelFile(glyph).addModel().condition(PlinthBlock.MODE, mode.ordinal()).end();
		}
		for (int level = 1; level <= PlinthVisual.GAUGE_MAX; level++) {
			ModelFile gauge = PlinthModels.gauge(models().getBuilder("plinth_gauge_" + level), level);
			Integer[] shown = new Integer[PlinthVisual.GAUGE_MAX - level + 1];
			for (int i = 0; i < shown.length; i++) {
				shown[i] = level + i;
			}
			state.part().modelFile(gauge).addModel().condition(PlinthBlock.GAUGE, shown).end();
		}

		// the inventory model has no block entity to wear anything, so it carries its own ash body
		BlockModelBuilder item = models().getBuilder("plinth_item").parent(PlinthModels.block());
		PlinthModels.fixed(item);
		PlinthModels.stoneBody(item);
		PlinthModels.worn(item);
		PlinthModels.glyph(item, ResourceMode.ITEM.ordinal());
	}
}
