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
		// the plinth core is a 3d model written by hand, the link tool is a handheld sprite with a lit variant
		ItemModelBuilder lit = awl("link_tool_lit");
		awl("link_tool").override().predicate(modLoc("linked"), 1F).model(lit).end();
	}

	// smaller in first person so the whole awl and its thread fit, stood up in third person so the face shows
	// from the front and the back with the grip in the fist
	private ItemModelBuilder awl(String name) {
		ItemModelBuilder model = singleTexture(name, mcLoc("item/handheld"), "layer0", modLoc("item/" + name));
		model.transforms()
				.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, -90, 25).translation(0, 3.8F, -0.4F).scale(0.5F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, -25).translation(0, 3.8F, -0.4F).scale(0.5F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(90, 0, 0).translation(3.4F, -1.2F, 3.45F).scale(0.85F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(90, 0, 0).translation(-3.4F, -1.2F, 3.45F).scale(0.85F).end();
		return model;
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
