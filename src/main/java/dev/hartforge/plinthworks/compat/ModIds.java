package dev.hartforge.plinthworks.compat;

import net.neoforged.fml.ModList;

public class ModIds
{
	public static final String CREATE = "create";
	public static final String JEI = "jei";
	public static final String EMI = "emi";

	public static boolean loaded(String id) {
		return ModList.get().isLoaded(id);
	}
}
