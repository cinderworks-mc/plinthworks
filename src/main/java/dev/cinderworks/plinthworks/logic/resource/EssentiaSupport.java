package dev.cinderworks.plinthworks.logic.resource;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.ToIntFunction;

// plinthworks keeps essentia as ids and numbers and knows no magic mod. a compat registers a provider here.
// with none registered the mode is never offered, and a plinth that was already in it (a world from before a mod
// was removed) idles with its points kept
public class EssentiaSupport
{
	private static EssentiaProvider provider;
	private static EssentiaResourceType type;
	private static Function<ResourceLocation, Component> names = id -> Component.literal(id.toString());
	private static ToIntFunction<ResourceLocation> colors = id -> 0;

	public static boolean available() {
		return provider != null;
	}

	public static EssentiaProvider provider() {
		return provider;
	}

	// null takes it away again, the gametests swap providers per batch
	public static void register(EssentiaProvider essentia) {
		provider = essentia;
		type = essentia == null ? null : new EssentiaResourceType(essentia);
	}

	public static void registerClient(Function<ResourceLocation, Component> name, ToIntFunction<ResourceLocation> color) {
		names = name;
		colors = color;
	}

	static EssentiaResourceType type() {
		return type;
	}

	public static Component name(ResourceLocation aspect) {
		return names.apply(aspect);
	}

	// 0 means no colour known, the gauge falls back to grey
	public static int color(ResourceLocation aspect) {
		return colors.applyAsInt(aspect);
	}
}
