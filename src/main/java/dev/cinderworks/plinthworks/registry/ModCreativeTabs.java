package dev.cinderworks.plinthworks.registry;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs
{
	public static final DeferredRegister<CreativeModeTab> TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Plinthworks.MODID);

	public static final Supplier<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.plinthworks"))
			.icon(() -> new ItemStack(ModItems.PLINTH.get()))
			.displayItems((params, out) -> {
				out.accept(ModItems.PLINTH.get());
				out.accept(ModItems.PLINTH_CORE.get());
				out.accept(ModItems.SIGIL_BASE.get());
				out.accept(ModItems.ETCHING_BASE.get());
				out.accept(ModItems.SEAL_BASE.get());
				for (SigilType type : SigilType.values()) {
					out.accept(ModItems.SIGILS.get(type).get());
				}
				for (EtchingType type : EtchingType.values()) {
					for (EtchingTier tier : EtchingTier.values()) {
						out.accept(ModItems.ETCHINGS.get(type).get(tier).get());
					}
				}
				for (SealType type : SealType.values()) {
					out.accept(ModItems.SEALS.get(type).get());
				}
				out.accept(ModItems.LINK_TOOL.get());
			}).build());
}
