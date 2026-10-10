package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.registry.ModBlocks;
import dev.cinderworks.plinthworks.registry.ModItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

// only vanilla triggers, so steps that need a custom trigger (linking two plinths, moving items) are left out
public class ModAdvancements implements AdvancementProvider.AdvancementGenerator
{
	@Override
	public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
			ExistingFileHelper files) {
		ItemLike[] sigils = ModItems.SIGILS.values().stream().map(item -> (ItemLike) item.get()).toArray(ItemLike[]::new);
		ItemLike[] etchings = ModItems.ETCHINGS.values().stream().flatMap(tiers -> tiers.values().stream())
				.map(item -> (ItemLike) item.get()).toArray(ItemLike[]::new);
		ItemLike[] seals = ModItems.SEALS.values().stream().map(item -> (ItemLike) item.get()).toArray(ItemLike[]::new);
		ItemLike[] topEtchings = ModItems.ETCHINGS.values().stream().map(tiers -> (ItemLike) tiers.get(EtchingTier.T4).get())
				.toArray(ItemLike[]::new);

		AdvancementHolder root = add(saver, null, "root", ModItems.PLINTH_CORE.get(), AdvancementType.TASK,
				"has_core", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PLINTH_CORE.get()));
		AdvancementHolder crafted = add(saver, root, "plinth_crafted", ModItems.PLINTH.get(), AdvancementType.TASK,
				"has_plinth", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PLINTH.get()));
		AdvancementHolder placed = add(saver, crafted, "plinth_placed", ModItems.PLINTH.get(), AdvancementType.TASK,
				"placed_plinth", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.PLINTH.get()));
		AdvancementHolder sigil = add(saver, placed, "sigil", ModItems.SIGILS.get(SigilType.IMPORT).get(),
				AdvancementType.TASK, "used_sigil", usedOnPlinth(sigils));
		add(saver, sigil, "void", ModItems.SIGILS.get(SigilType.VOID).get(), AdvancementType.TASK,
				"has_void", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SIGILS.get(SigilType.VOID).get()));
		AdvancementHolder etching = add(saver, placed, "etching", ModItems.ETCHINGS.get(EtchingType.RANGE).get(EtchingTier.T1).get(),
				AdvancementType.TASK, "used_etching", usedOnPlinth(etchings));
		add(saver, etching, "tier_four", ModItems.ETCHINGS.get(EtchingType.SPEED).get(EtchingTier.T4).get(),
				AdvancementType.GOAL, "has_tier_four", InventoryChangeTrigger.TriggerInstance.hasItems(
						ItemPredicate.Builder.item().of(topEtchings)));
		add(saver, placed, "seal", ModItems.SEALS.get(SealType.TAG).get(), AdvancementType.TASK,
				"used_seal", usedOnPlinth(seals));
		add(saver, placed, "link_tool", ModItems.LINK_TOOL.get(), AdvancementType.TASK,
				"has_link_tool", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.LINK_TOOL.get()));
	}

	private static Criterion<?> usedOnPlinth(ItemLike... items) {
		return ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
				LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(ModBlocks.PLINTH.get())),
				ItemPredicate.Builder.item().of(items));
	}

	private static AdvancementHolder add(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String id,
			ItemLike icon, AdvancementType type, String name, Criterion<?> criterion) {
		ResourceLocation key = Plinthworks.id("main/" + id);
		Advancement.Builder builder = Advancement.Builder.advancement()
				.display(new ItemStack(icon),
						Component.translatable("advancements.plinthworks." + id + ".title"),
						Component.translatable("advancements.plinthworks." + id + ".description"),
						parent == null ? ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png") : null,
						type, true, false, false)
				.addCriterion(name, criterion);
		if (parent != null) {
			builder.parent(parent);
		}
		AdvancementHolder holder = builder.build(key);
		saver.accept(holder);
		return holder;
	}
}
