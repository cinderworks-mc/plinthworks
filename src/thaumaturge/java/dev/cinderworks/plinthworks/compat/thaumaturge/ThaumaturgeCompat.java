package dev.cinderworks.plinthworks.compat.thaumaturge;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.compat.ModIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// the only class here that loads without thaumaturge: it names no thaumaturge type and only touches the hooks
// once the mod is there. the source set is built against a local jar (libs/README.md), the core never sees it
@EventBusSubscriber(modid = Plinthworks.MODID)
public class ThaumaturgeCompat
{
	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event) {
		if (ModIds.loaded(ModIds.THAUMATURGE)) {
			event.enqueueWork(ThaumaturgeHooks::init);
		}
	}

	@EventBusSubscriber(modid = Plinthworks.MODID, value = Dist.CLIENT)
	public static class Client
	{
		@SubscribeEvent
		public static void setup(FMLClientSetupEvent event) {
			if (ModIds.loaded(ModIds.THAUMATURGE)) {
				event.enqueueWork(ThaumaturgeClientHooks::init);
			}
		}
	}
}
