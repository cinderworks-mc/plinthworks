package dev.hartforge.plinthworks.registry;

import dev.hartforge.plinthworks.Plinthworks;
import dev.hartforge.plinthworks.item.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;

import java.util.EnumMap;
import java.util.Map;

public class ModItems
{
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Plinthworks.MODID);

	public static final DeferredItem<BlockItem> PLINTH = ITEMS.registerSimpleBlockItem("plinth", ModBlocks.PLINTH);
	public static final DeferredItem<Item> PLINTH_CORE = ITEMS.registerSimpleItem("plinth_core");
	public static final DeferredItem<Item> SIGIL_BASE = ITEMS.registerSimpleItem("sigil_base");
	public static final DeferredItem<Item> ETCHING_BASE = ITEMS.registerSimpleItem("etching_base");
	public static final DeferredItem<Item> SEAL_BASE = ITEMS.registerSimpleItem("seal_base");
	public static final DeferredItem<LinkToolItem> LINK_TOOL = ITEMS.register("link_tool", LinkToolItem::new);
	public static final Map<SigilType, DeferredItem<SigilItem>> SIGILS = registerSigils();
	public static final Map<EtchingType, Map<EtchingTier, DeferredItem<EtchingItem>>> ETCHINGS = registerEtchings();
	public static final Map<SealType, DeferredItem<SealItem>> SEALS = registerSeals();

	private static Map<SigilType, DeferredItem<SigilItem>> registerSigils() {
		EnumMap<SigilType, DeferredItem<SigilItem>> sigils = new EnumMap<>(SigilType.class);
		for (SigilType type : SigilType.values()) {
			sigils.put(type, ITEMS.register(type.path(), () -> new SigilItem(type)));
		}
		return sigils;
	}

	private static Map<EtchingType, Map<EtchingTier, DeferredItem<EtchingItem>>> registerEtchings() {
		EnumMap<EtchingType, Map<EtchingTier, DeferredItem<EtchingItem>>> etchings =
				new EnumMap<>(EtchingType.class);
		for (EtchingType type : EtchingType.values()) {
			EnumMap<EtchingTier, DeferredItem<EtchingItem>> tiers = new EnumMap<>(EtchingTier.class);
			for (EtchingTier tier : EtchingTier.values()) {
				tiers.put(tier, ITEMS.register(type.path(tier), () -> new EtchingItem(type, tier)));
			}
			etchings.put(type, tiers);
		}
		return etchings;
	}

	private static Map<SealType, DeferredItem<SealItem>> registerSeals() {
		EnumMap<SealType, DeferredItem<SealItem>> seals = new EnumMap<>(SealType.class);
		for (SealType type : SealType.values()) {
			seals.put(type, ITEMS.register(type.path(), () -> new SealItem(type)));
		}
		return seals;
	}
}
