package dev.hartforge.plinthworks.data;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.item.*;
import dev.hartforge.plinthworks.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider
{
	public ModItemModelProvider(PackOutput output, ExistingFileHelper files) {
		super(output, Plinthworks.MODID, files);
	}

	@Override
	protected void registerModels() {
		withExistingParent("plinth", modLoc("block/plinth"));
		singleTexture("plinth_core", mcLoc("item/generated"), "layer0", mcLoc("item/iron_ingot"));
		singleTexture("sigil_base", mcLoc("item/generated"), "layer0", mcLoc("item/copper_ingot"));
		singleTexture("etching_base", mcLoc("item/generated"), "layer0", mcLoc("item/iron_nugget"));
		singleTexture("seal_base", mcLoc("item/generated"), "layer0", mcLoc("item/paper"));
		for (SigilType type : SigilType.values()) {
			basicItem(ModItems.SIGILS.get(type).get());
		}
		for (EtchingType type : EtchingType.values()) {
			for (EtchingTier tier : EtchingTier.values()) {
				basicItem(ModItems.ETCHINGS.get(type).get(tier).get());
			}
		}
		for (SealType type : SealType.values()) {
			basicItem(ModItems.SEALS.get(type).get());
		}
		basicItem(ModItems.LINK_TOOL.get());
	}
}
