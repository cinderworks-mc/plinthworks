package dev.cinderworks.plinthworks.logic.resource;

import dev.cinderworks.plinthworks.logic.ResourceMode;

public class ResourceTypes
{
	public static final ItemResourceType ITEM = new ItemResourceType();
	public static final EnergyResourceType ENERGY = new EnergyResourceType();
	public static final FluidResourceType FLUID = new FluidResourceType(stack -> true);
	public static final FluidResourceType XP = new FluidResourceType(FluidResourceType::isXpFluid);

	public static ResourceType<?, ?> get(ResourceMode mode) {
		return switch (mode) {
			case ITEM -> ITEM;
			case ENERGY -> ENERGY;
			case FLUID -> FLUID;
			case XP -> XP;
		};
	}
}
