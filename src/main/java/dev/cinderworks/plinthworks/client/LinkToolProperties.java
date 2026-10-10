package dev.cinderworks.plinthworks.client;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.component.LinkBinding;
import dev.cinderworks.plinthworks.registry.ModDataComponents;
import dev.cinderworks.plinthworks.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Plinthworks.MODID, value = Dist.CLIENT)
public class LinkToolProperties
{
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		// lit once the tool holds a first plinth
		event.enqueueWork(() -> ItemProperties.register(ModItems.LINK_TOOL.get(),
				ResourceLocation.fromNamespaceAndPath(Plinthworks.MODID, "linked"),
				(stack, level, entity, seed) ->
						stack.getOrDefault(ModDataComponents.LINK_BINDING.get(), LinkBinding.EMPTY).anchor().isPresent() ? 1F : 0F));
	}
}
