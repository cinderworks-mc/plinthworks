package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.Plinthworks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

// second entry point of the mod, only on the classpath of the dev runs
@Mod(Plinthworks.MODID)
public class PlinthworksGameTestEntry
{
	public PlinthworksGameTestEntry(IEventBus modBus) {
		modBus.addListener(PlinthworksGameTestEntry::register);
	}

	private static void register(RegisterGameTestsEvent event) {
		event.register(BufferGameTests.class);
		event.register(GaugeGameTests.class);
	}
}
