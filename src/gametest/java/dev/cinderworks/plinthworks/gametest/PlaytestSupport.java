package dev.cinderworks.plinthworks.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.HashMap;
import java.util.Map;

// gold blocks stand in for an energy machine, so the FE path can be driven without another mod
final class PlaytestSupport
{
	static final Map<BlockPos, EnergyStorage> BATTERIES = new HashMap<>();

	private PlaytestSupport() {}

	static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, be, side) -> {
			return (IEnergyStorage) BATTERIES.computeIfAbsent(pos.immutable(), key -> new EnergyStorage(100000));
		}, Blocks.GOLD_BLOCK);
	}

	static EnergyStorage battery(net.minecraft.gametest.framework.GameTestHelper helper, BlockPos rel, int stored) {
		helper.setBlock(rel, Blocks.GOLD_BLOCK);
		BlockPos abs = helper.absolutePos(rel).immutable();
		EnergyStorage storage = new EnergyStorage(100000, 100000, 100000, stored);
		BATTERIES.put(abs, storage);
		return storage;
	}
}
