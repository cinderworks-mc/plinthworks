package dev.cinderworks.plinthworks.client;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.client.screen.PwScreens;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

// dark fill and a brass border on our own screens only
@EventBusSubscriber(modid = Plinthworks.MODID, value = Dist.CLIENT)
public class PwTooltipSkin
{
	private static final int FILL = 0xF017161A;
	private static final int BORDER_TOP = 0xFF8A6A34;
	private static final int BORDER_BOTTOM = 0xFF6B5128;

	@SubscribeEvent
	public static void colors(RenderTooltipEvent.Color event) {
		if (!PwScreens.isOurs(Minecraft.getInstance().screen)) {
			return;
		}
		event.setBackgroundStart(FILL);
		event.setBackgroundEnd(FILL);
		event.setBorderStart(BORDER_TOP);
		event.setBorderEnd(BORDER_BOTTOM);
	}
}
