package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.*;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider
{
	public ModItemModelProvider(PackOutput output, ExistingFileHelper files) {
		super(output, Plinthworks.MODID, files);
	}

	@Override
	protected void registerModels() {
		withExistingParent("plinth", modLoc("block/plinth_item"));
		flat("sigil_base");
		flat("etching_base");
		flat("seal_base");
		for (SigilType type : SigilType.values()) {
			flat(type.path());
		}
		for (EtchingType type : EtchingType.values()) {
			for (EtchingTier tier : EtchingTier.values()) {
				flat(type.path(tier));
			}
		}
		for (SealType type : SealType.values()) {
			flat(type.path());
		}
		// the plinth core and the link tool have their own hand written models
	}

	// flat tokens are held smaller than a vanilla item and stood up beside the hand so the face shows from the front in third person
	private void flat(String name) {
		ItemModelBuilder model = singleTexture(name, mcLoc("item/generated"), "layer0", modLoc("item/" + name));
		model.transforms()
				.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, -90, 25).translation(0.2F, 3, 1.13F).scale(0.4F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, -25).translation(0.2F, 3, 1.13F).scale(0.4F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(90, 0, 0).translation(3.6F, 2.5F, -0.6F).scale(0.55F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(90, 0, 0).translation(-3.6F, 2.5F, -0.6F).scale(0.55F).end();
	}
}
