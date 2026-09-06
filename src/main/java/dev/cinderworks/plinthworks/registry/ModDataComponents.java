package dev.cinderworks.plinthworks.registry;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.component.PlinthContents;
import dev.cinderworks.plinthworks.component.SealConfig;
import dev.cinderworks.plinthworks.component.LinkBinding;
import dev.cinderworks.plinthworks.component.PlinthLoadout;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents
{
	public static final DeferredRegister.DataComponents COMPONENTS =
			DeferredRegister.createDataComponents(Plinthworks.MODID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlinthContents>> PLINTH_CONTENTS =
			COMPONENTS.registerComponentType("plinth_contents",
					builder -> builder.persistent(PlinthContents.CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SealConfig>> SEAL_CONFIG =
			COMPONENTS.registerComponentType("seal_config", builder -> builder.persistent(SealConfig.CODEC)
					.networkSynchronized(SealConfig.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<LinkBinding>> LINK_BINDING =
			COMPONENTS.registerComponentType("link_binding", builder -> builder.persistent(LinkBinding.CODEC)
					.networkSynchronized(LinkBinding.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlinthLoadout>> PLINTH_LOADOUT =
			COMPONENTS.registerComponentType("plinth_loadout",
					builder -> builder.persistent(PlinthLoadout.CODEC));
}
