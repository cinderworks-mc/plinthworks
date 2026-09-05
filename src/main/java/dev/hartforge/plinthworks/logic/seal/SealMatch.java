package dev.hartforge.plinthworks.logic.seal;

import dev.hartforge.plinthworks.component.*;
import dev.hartforge.plinthworks.item.SealType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

import java.util.List;

public class SealMatch
{
	public static boolean matches(SealType type, SealConfig config, ItemStack candidate) {
		boolean found = config.keys().stream().anyMatch(key -> matchesKey(type, key, candidate));
		return config.whitelist() == found;
	}

	public static boolean bankAllows(List<SealEntry> seals, ItemStack candidate) {
		return bankAllowsResults(seals.stream()
				.map(seal -> new SealResult(seal.config().whitelist(),
						matches(seal.type(), seal.config(), candidate))).toList());
	}

	public static boolean bankAllowsResults(List<SealResult> results) {
		boolean hasWhitelist = false;
		boolean allowed = false;
		for (SealResult result : results) {
			if (result.whitelist()) {
				hasWhitelist = true;
				allowed |= result.match();
			} else if (!result.match()) {
				return false;
			}
		}
		return !hasWhitelist || allowed;
	}

	private static boolean matchesKey(SealType type, MatchKey key, ItemStack candidate) {
		return switch (type) {
			case ITEM -> key instanceof MatchKey.ItemKey item
					&& BuiltInRegistries.ITEM.getKey(candidate.getItem()).equals(item.item());
			case ITEM_EXACT -> key instanceof MatchKey.ComponentKey exact
					&& exact.value().equals(componentFingerprint(candidate));
			case MOD -> key instanceof MatchKey.ModKey mod
					&& BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace().equals(mod.namespace());
			case TAG -> key instanceof MatchKey.TagKey tag
					&& candidate.is(TagKey.create(Registries.ITEM, tag.tag()));
			case COMPONENT -> key instanceof MatchKey.ComponentKey component
					&& componentMatches(component, candidate);
		};
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean componentMatches(MatchKey.ComponentKey key, ItemStack stack) {
		DataComponentType type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(key.component());
		Object value = stack.get(type);
		return value != null && key.value().equals(String.valueOf(value));
	}

	public static String componentFingerprint(ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "|" + stack.getComponentsPatch();
	}

	public record SealEntry(SealType type, SealConfig config) {}

	public record SealResult(boolean whitelist, boolean match) {}
}
