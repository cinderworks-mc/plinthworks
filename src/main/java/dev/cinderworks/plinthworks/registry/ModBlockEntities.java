package dev.cinderworks.plinthworks.registry;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Plinthworks.MODID);

	public static final Supplier<BlockEntityType<PlinthBlockEntity>> PLINTH = BLOCK_ENTITIES.register("plinth",
			() -> BlockEntityType.Builder.of(PlinthBlockEntity::new, ModBlocks.PLINTH.get()).build(null));

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PLINTH.get(),
				(be, side) -> be.getAutomationHandler());
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PLINTH.get(),
				(be, side) -> be.energy());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, PLINTH.get(),
				(be, side) -> be.fluid());
	}
}
