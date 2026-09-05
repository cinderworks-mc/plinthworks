package dev.hartforge.plinthworks.client;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.client.renderer.PlinthRenderer;
import dev.hartforge.plinthworks.client.screen.PlinthScreen;
import dev.hartforge.plinthworks.registry.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Plinthworks.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents
{
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlockEntities.PLINTH.get(), PlinthRenderer::new);
	}

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenus.PLINTH.get(), PlinthScreen::new);
	}
}
