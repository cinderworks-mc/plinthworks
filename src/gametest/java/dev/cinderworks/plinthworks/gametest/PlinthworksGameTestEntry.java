package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.logic.sigil.PlacementGameTests;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

// second entry point of the mod, only on the classpath of the dev runs
@Mod(Plinthworks.MODID)
public class PlinthworksGameTestEntry
{
	public PlinthworksGameTestEntry(IEventBus modBus) {
		modBus.addListener(PlinthworksGameTestEntry::register);
		modBus.addListener(PlaytestSupport::registerCapabilities);
	}

	private static void register(RegisterGameTestsEvent event) {
		event.register(BufferGameTests.class);
		event.register(GaugeGameTests.class);
		event.register(ShiftClickGameTests.class);
		event.register(PersistenceGameTests.class);
		event.register(VisualGameTests.class);
		event.register(SealGameTests.class);
		event.register(NetworkGameTests.class);
		event.register(TransferGameTests.class);
		event.register(BreakerGameTests.class);
		event.register(PlaytestGameTests.class);
		event.register(PlacementGameTests.class);
		event.register(EssentiaGameTests.class);
	}
}
