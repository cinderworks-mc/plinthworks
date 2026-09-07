package dev.cinderworks.plinthworks.logic.resource;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.logic.UpgradeSet;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.function.Predicate;

public class EnergyResourceType implements ResourceType<IEnergyStorage, Integer>
{
	@Override
	public IEnergyStorage find(Level level, BlockPos pos, Direction side) {
		return level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
	}

	@Override
	public IEnergyStorage buffer(PlinthBlockEntity be) {
		return be.energy();
	}

	@Override
	public Predicate<Integer> filter(PlinthBlockEntity be, UpgradeSet upgrades) {
		return value -> true;
	}

	@Override
	public int move(IEnergyStorage source, IEnergyStorage dest, int amount, Predicate<Integer> filter) {
		int offered = source.extractEnergy(amount, true);
		int accepted = dest.receiveEnergy(offered, true);
		if (accepted <= 0) {
			return 0;
		}
		int extracted = source.extractEnergy(accepted, false);
		int moved = dest.receiveEnergy(extracted, false);
		if (moved < extracted) {
			source.receiveEnergy(extracted - moved, false);
		}
		return moved;
	}

	@Override
	public int discard(IEnergyStorage source, int amount, Predicate<Integer> filter) {
		return source.extractEnergy(amount, false);
	}

	@Override
	public int throughput(UpgradeSet upgrades) {
		long amount = ModConfig.ENERGY_BASE_TRANSFER.get()
				+ (long) upgrades.capacityLevel() * ModConfig.ENERGY_TRANSFER_STEP.get();
		return (int) Math.min(ModConfig.ENERGY_TRANSFER_MAX.get(), amount);
	}
}
