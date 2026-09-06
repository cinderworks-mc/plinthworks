package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Plinthworks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper files = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

		generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, files));
		generator.addProvider(event.includeClient(), new ModItemModelProvider(output, files));
		generator.addProvider(event.includeClient(), new ModLangProvider(output));
		generator.addProvider(event.includeServer(), new ModRecipeProvider(output, registries));
		generator.addProvider(event.includeServer(), new ModLootProvider(output, registries));
		generator.addProvider(event.includeServer(), new ModTagProvider(output, registries, files));
	}
}
