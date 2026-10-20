package dev.cinderworks.plinthworks.logic.resource;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.logic.UpgradeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class EssentiaResourceType implements ResourceType<EssentiaResourceType.Vessel, ResourceLocation>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EssentiaResourceType.class);

	// where the points sit, so anything with nowhere to go is released there and not just dropped
	public record Vessel(EssentiaHandler handler, Level level, BlockPos pos) {}

	private final EssentiaProvider provider;

	public EssentiaResourceType(EssentiaProvider provider) {
		this.provider = provider;
	}

	@Override
	public Vessel find(Level level, BlockPos pos, Direction side) {
		// linked plinths are looked up through here too, their buffer is ours
		if (level.getBlockEntity(pos) instanceof PlinthBlockEntity plinth) {
			return buffer(plinth);
		}
		EssentiaHandler handler = provider.find(level, pos, side);
		return handler == null ? null : new Vessel(handler, level, pos);
	}

	@Override
	public Vessel buffer(PlinthBlockEntity be) {
		return new Vessel(be.essentia(), be.getLevel(), be.getBlockPos());
	}

	@Override
	public Predicate<ResourceLocation> filter(PlinthBlockEntity be, UpgradeSet upgrades) {
		return aspect -> true;
	}

	// same recipe as fluids: simulate both ends, then move, and hand back what the destination didn't take
	@Override
	public int move(Vessel source, Vessel dest, int amount, Predicate<ResourceLocation> filter) {
		int moved = 0;
		for (Essentia held : source.handler().contents()) {
			if (moved >= amount) {
				break;
			}
			ResourceLocation aspect = held.aspect();
			if (!filter.test(aspect)) {
				continue;
			}
			int offered = source.handler().extract(aspect, amount - moved, true);
			if (offered <= 0) {
				continue;
			}
			int accepted = dest.handler().insert(aspect, offered, true);
			if (accepted <= 0) {
				continue;
			}
			int taken = source.handler().extract(aspect, accepted, false);
			int put = dest.handler().insert(aspect, taken, false);
			if (put < taken) {
				int back = source.handler().insert(aspect, taken - put, false);
				int lost = taken - put - back;
				if (lost > 0) {
					LOGGER.warn("{} points of {} had nowhere to go at {}, released", lost, aspect, source.pos());
					release(source, aspect, lost);
				}
			}
			moved += put;
		}
		return moved;
	}

	// a void sigil empties its own buffer, and what leaves it goes back to the world through the provider
	@Override
	public int discard(Vessel source, int amount, Predicate<ResourceLocation> filter) {
		int voided = 0;
		for (Essentia held : source.handler().contents()) {
			if (voided < amount && filter.test(held.aspect())) {
				int taken = source.handler().extract(held.aspect(), amount - voided, false);
				release(source, held.aspect(), taken);
				voided += taken;
			}
		}
		return voided;
	}

	@Override
	public int throughput(UpgradeSet upgrades) {
		long amount = ModConfig.ESSENTIA_BASE_TRANSFER.get()
				+ (long) upgrades.capacityLevel() * ModConfig.ESSENTIA_TRANSFER_STEP.get();
		return (int) Math.min(ModConfig.ESSENTIA_TRANSFER_MAX.get(), amount);
	}

	private void release(Vessel at, ResourceLocation aspect, int points) {
		if (points > 0) {
			provider.release(at.level(), at.pos(), aspect, points);
		}
	}
}
