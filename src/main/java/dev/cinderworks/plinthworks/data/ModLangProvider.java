package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.registry.*;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider
{
	private static final String[] ROMAN = {"", "I", "II", "III", "IV"};

	public ModLangProvider(PackOutput output) {
		super(output, Plinthworks.MODID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add(ModBlocks.PLINTH.get(), "Plinth");
		add(ModItems.PLINTH_CORE.get(), "Plinth Core");
		add(ModItems.SIGIL_BASE.get(), "Blank Sigil");
		add(ModItems.ETCHING_BASE.get(), "Blank Etching");
		add(ModItems.SEAL_BASE.get(), "Blank Seal");
		add("itemGroup.plinthworks", "Plinthworks");
		for (SigilType type : SigilType.values()) {
			add(ModItems.SIGILS.get(type).get(), title(type.name()) + " Sigil");
		}
		for (EtchingType type : EtchingType.values()) {
			for (EtchingTier tier : EtchingTier.values()) {
				add(ModItems.ETCHINGS.get(type).get(tier).get(),
						title(type.name()) + " Etching (Tier " + ROMAN[tier.level()] + ")");
			}
		}
		for (SealType type : SealType.values()) {
			add(ModItems.SEALS.get(type).get(), sealName(type));
		}
		add(ModItems.LINK_TOOL.get(), "Link Tool");
		add("plinthworks.sigil.import.desc", "Pulls resources from nearby blocks");
		add("plinthworks.sigil.export.desc", "Pushes buffered resources into nearby blocks");
		add("plinthworks.sigil.crafting.desc", "Crafts the displayed item from nearby ingredients");
		add("plinthworks.sigil.void.desc", "Deletes resources from the plinth buffer");
		add("plinthworks.sigil.breaker.desc", "Breaks blocks and routes their drops into nearby storage");
		add("plinthworks.sigil.placer.desc", "Places blocks pulled from nearby storage");
		add("plinthworks.sigil.planter.desc", "Plants seeds and saplings from nearby storage");
		add("plinthworks.sigil.harvester.desc", "Harvests mature crops and replants available seeds");
		add("plinthworks.sigil.magnet.desc", "Collects dropped items into nearby storage");
		add("plinthworks.etching.speed.desc", "Shortens the delay between operations");
		add("plinthworks.etching.capacity.desc", "Moves more resources per operation");
		add("plinthworks.etching.storage.desc", "Raises the plinth buffer limit");
		add("plinthworks.etching.range.desc", "Extends local search and linking reach");
		add("plinthworks.etching.tier", "Tier %s");
		add("plinthworks.seal.item.desc", "Matches items while ignoring their components");
		add("plinthworks.seal.item_exact.desc", "Matches an item and all of its components");
		add("plinthworks.seal.mod.desc", "Matches items from the same mod");
		add("plinthworks.seal.tag.desc", "Matches items in a selected tag");
		add("plinthworks.seal.component.desc", "Matches a selected item component");
		add("plinthworks.seal.whitelist", "Whitelist - %s keys");
		add("plinthworks.seal.blacklist", "Blacklist - %s keys");
		add("container.plinthworks.plinth", "Plinth");
		add("gui.plinthworks.sigil", "Sigil");
		add("gui.plinthworks.etchings", "Etchings");
		add("gui.plinthworks.seals", "Seals");
		add("gui.plinthworks.display", "Display");
		add("gui.plinthworks.stat_interval", "Rate: %st");
		add("gui.plinthworks.stat_throughput", "Move: %s");
		add("gui.plinthworks.stat_storage", "Hold: %s");
		add("gui.plinthworks.stat_energy_move", "Move: %s FE");
		add("gui.plinthworks.stat_energy", "%s / %s FE");
		add("gui.plinthworks.stat_fluid_move", "Move: %s mB");
		add("gui.plinthworks.stat_fluid", "%s / %s mB");
		add("gui.plinthworks.stat_xp", "%s XP (%s mB)");
		add("gui.plinthworks.fluid_empty", "Empty");
		add("gui.plinthworks.stat_range", "Reach: %s");
		add("gui.plinthworks.stat_channel", "%s (%s)");
		add("gui.plinthworks.seal_config", "Seal config");
		add("gui.plinthworks.no_seal", "No seal in this slot");
		add("gui.plinthworks.toggle_mode", "Toggle mode");
		add("gui.plinthworks.mode_whitelist", "Whitelist");
		add("gui.plinthworks.mode_blacklist", "Blacklist");
		add("gui.plinthworks.clear_keys", "Clear keys");
		add("gui.plinthworks.add_displayed", "Add displayed item");
		add("gui.plinthworks.whitelist", "Whitelist - %s keys");
		add("gui.plinthworks.blacklist", "Blacklist - %s keys");
		add("gui.plinthworks.redstone.always", "Always");
		add("gui.plinthworks.redstone.pause", "Pause");
		add("gui.plinthworks.redstone.run", "Run");
		add("gui.plinthworks.redstone.always.tip", "Redstone: always runs");
		add("gui.plinthworks.redstone.pause.tip", "Redstone: pauses when powered");
		add("gui.plinthworks.redstone.run.tip", "Redstone: runs only when powered");
		add("gui.plinthworks.mode.tip", "Network transfer mode");
		add("gui.plinthworks.resource.tip", "Resource mode");
		add("gui.plinthworks.mode.tip.state", "Transfer: %s");
		add("gui.plinthworks.resource.tip.state", "Resource: %s");
		add("gui.plinthworks.val_ticks", "%st");
		add("gui.plinthworks.val_fe", "%s FE");
		add("gui.plinthworks.val_mb", "%s mB");
		add("gui.plinthworks.network_mode", "Mode");
		add("gui.plinthworks.network_overview", "Network");
		add("gui.plinthworks.network_members", "Channel members");
		add("gui.plinthworks.network_empty", "No linked plinths");
		add("gui.plinthworks.network_name", "Name");
		add("gui.plinthworks.network_rename", "Rename");
		add("message.plinthworks.link_cleared", "Link binding cleared");
		add("message.plinthworks.link_bound", "Bound link tool to %s");
		add("message.plinthworks.link_joined", "Linked to channel %s");
		add("message.plinthworks.link_range", "That plinth is outside linking range");
	}

	private static String title(String value) {
		String lower = value.toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1).replace('_', ' ');
	}

	private static String sealName(SealType type) {
		return switch (type) {
			case ITEM -> "Item Seal";
			case ITEM_EXACT -> "Exact Item Seal";
			case MOD -> "Mod Seal";
			case TAG -> "Tag Seal";
			case COMPONENT -> "Component Seal";
		};
	}
}
