package dev.cinderworks.plinthworks.registry;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.menu.SealMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus
{
	public static final DeferredRegister<MenuType<?>> MENUS =
			DeferredRegister.create(Registries.MENU, Plinthworks.MODID);

	public static final Supplier<MenuType<PlinthMenu>> PLINTH = MENUS.register("plinth",
			() -> IMenuTypeExtension.create(PlinthMenu::new));
	public static final Supplier<MenuType<SealMenu>> SEAL = MENUS.register("seal",
			() -> IMenuTypeExtension.create(SealMenu::new));
}
