package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.*;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Locale;

public class ModItemModelProvider extends ItemModelProvider
{
	public ModItemModelProvider(PackOutput output, ExistingFileHelper files) {
		super(output, Plinthworks.MODID, files);
	}

	@Override
	protected void registerModels() {
		withExistingParent("plinth", modLoc("block/plinth_item"));
		flat("sigil_base");
		singleTexture("etching_base", mcLoc("item/generated"), "layer0", modLoc("item/etching_base"));
		singleTexture("seal_base", mcLoc("item/generated"), "layer0", modLoc("item/seal_base"));
		// etchings and seals are two flat layers, a plate and a glyph on top, so they get a little thickness
		for (SigilType type : SigilType.values()) {
			flat(type.path());
		}
		for (EtchingType type : EtchingType.values()) {
			String plate = "etching_plate_" + type.name().toLowerCase(Locale.ROOT);
			for (EtchingTier tier : EtchingTier.values()) {
				layered(type.path(tier), plate);
			}
		}
		for (SealType type : SealType.values()) {
			layered(type.path(), "seal_plate_" + sealCorner(type));
		}
		// the plinth core and the link tool have their own hand written models
	}

	// held a bit smaller than a vanilla flat item, and turned so the face shows from the front in third person
	private void layered(String name, String plate) {
		ItemModelBuilder model = withExistingParent(name, mcLoc("item/generated"))
				.texture("layer0", modLoc("item/" + plate))
				.texture("layer1", modLoc("item/" + name));
		model.transforms()
				.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, -90, 25).translation(1.13F, 3.2F, 1.13F).scale(0.32F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, -25).translation(1.13F, 3.2F, 1.13F).scale(0.32F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 0, 0).translation(0, 2, 2).scale(0.5F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 0, 0).translation(0, 2, 2).scale(0.5F).end();
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

	private static String sealCorner(SealType type) {
		return switch (type) {
			case ITEM -> "tl";
			case ITEM_EXACT -> "tr";
			case MOD -> "br";
			case TAG -> "bl";
			case COMPONENT -> "tlbr";
		};
	}
}
