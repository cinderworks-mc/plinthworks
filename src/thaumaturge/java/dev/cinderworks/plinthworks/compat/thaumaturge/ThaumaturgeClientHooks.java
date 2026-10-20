package dev.cinderworks.plinthworks.compat.thaumaturge;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import dev.cinderworks.plinthworks.logic.resource.EssentiaSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ThaumaturgeClientHooks
{
	public static void init() {
		EssentiaSupport.registerClient(ThaumaturgeClientHooks::name, ThaumaturgeClientHooks::color);
	}

	// masked like thaumaturge's own tooltips until the player has discovered the aspect
	private static Component name(ResourceLocation id) {
		Holder<IAspect> aspect = resolve(id);
		return aspect == null ? Component.literal(id.toString()) : AspectComponents.name(aspect);
	}

	private static int color(ResourceLocation id) {
		Holder<IAspect> aspect = resolve(id);
		return aspect == null ? 0 : aspect.value().color();
	}

	private static Holder<IAspect> resolve(ResourceLocation id) {
		Minecraft mc = Minecraft.getInstance();
		return mc.level == null ? null : Aspects.resolve(mc.level, ResourceKey.create(IAspect.REGISTRY_KEY, id));
	}
}
