package dev.cinderworks.plinthworks.logic.resource;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import dev.cinderworks.plinthworks.logic.UpgradeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class ResourceTypes
{
	public static final ItemResourceType ITEM = new ItemResourceType();
	public static final EnergyResourceType ENERGY = new EnergyResourceType();
	public static final FluidResourceType FLUID = new FluidResourceType(stack -> true);
	public static final FluidResourceType XP = new FluidResourceType(FluidResourceType::isXpFluid);
	// essentia mode with nothing behind it: finds nothing, moves nothing, voids nothing
	static final ResourceType<Object, Object> IDLE = new ResourceType<>() {
		@Override
		public Object find(Level level, BlockPos pos, Direction side) {
			return null;
		}

		@Override
		public Object buffer(PlinthBlockEntity be) {
			return null;
		}

		@Override
		public Predicate<Object> filter(PlinthBlockEntity be, UpgradeSet upgrades) {
			return value -> false;
		}

		@Override
		public int move(Object source, Object dest, int amount, Predicate<Object> filter) {
			return 0;
		}

		@Override
		public int discard(Object source, int amount, Predicate<Object> filter) {
			return 0;
		}

		@Override
		public int throughput(UpgradeSet upgrades) {
			return 0;
		}

		@Override
		public List<Object> nearby(PlinthBlockEntity be, int range) {
			return List.of();
		}
	};

	public static ResourceType<?, ?> get(ResourceMode mode) {
		return switch (mode) {
			case ITEM -> ITEM;
			case ENERGY -> ENERGY;
			case FLUID -> FLUID;
			case XP -> XP;
			case ESSENTIA -> EssentiaSupport.available() ? EssentiaSupport.type() : IDLE;
		};
	}
}
