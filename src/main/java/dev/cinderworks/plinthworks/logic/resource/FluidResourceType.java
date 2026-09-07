package dev.cinderworks.plinthworks.logic.resource;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.logic.UpgradeSet;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.function.Predicate;

public class FluidResourceType implements ResourceType<IFluidHandler, FluidStack>
{
	private final Predicate<FluidStack> filter;

	public FluidResourceType(Predicate<FluidStack> filter) {
		this.filter = filter;
	}

	@Override
	public IFluidHandler find(Level level, BlockPos pos, Direction side) {
		return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
	}

	@Override
	public IFluidHandler buffer(PlinthBlockEntity be) {
		return be.fluid();
	}

	@Override
	public Predicate<FluidStack> filter(PlinthBlockEntity be, UpgradeSet upgrades) {
		return filter;
	}

	@Override
	public int move(IFluidHandler source, IFluidHandler dest, int amount, Predicate<FluidStack> filter) {
		int moved = 0;
		for (int tank = 0; tank < source.getTanks() && moved < amount; tank++) {
			FluidStack shown = source.getFluidInTank(tank);
			if (shown.isEmpty() || !filter.test(shown)) {
				continue;
			}
			FluidStack offered = source.drain(shown.copyWithAmount(amount - moved),
					IFluidHandler.FluidAction.SIMULATE);
			if (offered.isEmpty()) {
				continue;
			}
			int accepted = dest.fill(offered, IFluidHandler.FluidAction.SIMULATE);
			if (accepted <= 0) {
				continue;
			}
			FluidStack extracted = source.drain(offered.copyWithAmount(accepted),
					IFluidHandler.FluidAction.EXECUTE);
			int filled = dest.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
			if (filled < extracted.getAmount()) {
				source.fill(extracted.copyWithAmount(extracted.getAmount() - filled),
						IFluidHandler.FluidAction.EXECUTE);
			}
			moved += filled;
		}
		return moved;
	}

	@Override
	public int discard(IFluidHandler source, int amount, Predicate<FluidStack> filter) {
		int moved = 0;
		for (int tank = 0; tank < source.getTanks() && moved < amount; tank++) {
			FluidStack shown = source.getFluidInTank(tank);
			if (!shown.isEmpty() && filter.test(shown)) {
				moved += source.drain(shown.copyWithAmount(amount - moved),
						IFluidHandler.FluidAction.EXECUTE).getAmount();
			}
		}
		return moved;
	}

	@Override
	public int throughput(UpgradeSet upgrades) {
		long amount = ModConfig.FLUID_BASE_TRANSFER.get()
				+ (long) upgrades.capacityLevel() * ModConfig.FLUID_TRANSFER_STEP.get();
		return (int) Math.min(ModConfig.FLUID_TRANSFER_MAX.get(), amount);
	}

	static boolean isXpFluid(FluidStack stack) {
		ResourceLocation fluid = BuiltInRegistries.FLUID.getKey(stack.getFluid());
		for (String entry : ModConfig.XP_FLUIDS.get()) {
			boolean tag = entry.startsWith("#");
			ResourceLocation id = ResourceLocation.tryParse(tag ? entry.substring(1) : entry);
			if (id == null) {
				continue;
			}
			if ((tag && stack.is(TagKey.create(Registries.FLUID, id))) || (!tag && id.equals(fluid))) {
				return true;
			}
		}
		return false;
	}
}
