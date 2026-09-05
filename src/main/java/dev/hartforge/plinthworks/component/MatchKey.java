package dev.hartforge.plinthworks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public sealed interface MatchKey permits MatchKey.ItemKey, MatchKey.TagKey,
		MatchKey.ModKey, MatchKey.ComponentKey
{
	Codec<MatchKey> CODEC = Codec.STRING.dispatch("type", MatchKey::type, type -> switch (type) {
		case "tag" -> TagKey.CODEC;
		case "mod" -> ModKey.CODEC;
		case "component" -> ComponentKey.CODEC;
		default -> ItemKey.CODEC;
	});

	String type();

	record ItemKey(ResourceLocation item) implements MatchKey
	{
		private static final MapCodec<ItemKey> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("item").forGetter(ItemKey::item)
		).apply(instance, ItemKey::new));

		@Override
		public String type() {
			return "item";
		}
	}

	record TagKey(ResourceLocation tag) implements MatchKey
	{
		private static final MapCodec<TagKey> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("tag").forGetter(TagKey::tag)
		).apply(instance, TagKey::new));

		@Override
		public String type() {
			return "tag";
		}
	}

	record ModKey(String namespace) implements MatchKey
	{
		private static final MapCodec<ModKey> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.fieldOf("namespace").forGetter(ModKey::namespace)
		).apply(instance, ModKey::new));

		@Override
		public String type() {
			return "mod";
		}
	}

	record ComponentKey(ResourceLocation component, String value) implements MatchKey
	{
		private static final MapCodec<ComponentKey> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("component").forGetter(ComponentKey::component),
				Codec.STRING.fieldOf("value").forGetter(ComponentKey::value)
		).apply(instance, ComponentKey::new));

		@Override
		public String type() {
			return "component";
		}
	}
}
