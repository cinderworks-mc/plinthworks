package dev.hartforge.plinthworks;

import dev.hartforge.plinthworks.compat.CompatHooks;
import dev.hartforge.plinthworks.config.ModConfig;
import dev.hartforge.plinthworks.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.neoforged.fml.config.ModConfig.Type;

@Mod(Plinthworks.MODID)
public class Plinthworks
{
	public static final String MODID = "plinthworks";

	public Plinthworks(IEventBus modBus, ModContainer container) {
		ModBlocks.BLOCKS.register(modBus);
		ModItems.ITEMS.register(modBus);
		ModBlockEntities.BLOCK_ENTITIES.register(modBus);
		ModCreativeTabs.TABS.register(modBus);
		ModDataComponents.COMPONENTS.register(modBus);
		ModRecipes.SERIALIZERS.register(modBus);
		ModMenus.MENUS.register(modBus);
		modBus.addListener(ModBlockEntities::registerCapabilities);
		container.registerConfig(Type.COMMON, ModConfig.SPEC);
		CompatHooks.init();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
