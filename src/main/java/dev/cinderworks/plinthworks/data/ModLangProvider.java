package dev.cinderworks.plinthworks.data;

import dev.cinderworks.plinthworks.Plinthworks;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.registry.*;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Locale;

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
						etchingStat(type) + " Etching " + ROMAN[tier.level()]);
			}
		}
		for (SealType type : SealType.values()) {
			add(ModItems.SEALS.get(type).get(), sealName(type));
		}
		add(ModItems.LINK_TOOL.get(), "Link Tool");
		advancement("root", "Plinthworks", "Craft a plinth core from iron and stone slabs");
		advancement("plinth_crafted", "Solid Footing", "Craft a plinth from a core and four matching blocks");
		advancement("plinth_placed", "Take a Stand", "Place a plinth anywhere in the world");
		advancement("sigil", "Teach It Something", "Use a sigil on a plinth to open its screen");
		advancement("void", "Black Hole", "Craft a void sigil to empty a plinth buffer");
		advancement("etching", "Fine Print", "Use an etching on a plinth to tune it");
		advancement("tier_four", "Tier IV", "Craft an etching at tier IV");
		advancement("seal", "Fussy Eater", "Use a seal on a plinth to filter it");
		advancement("link_tool", "Same Wavelength", "Craft a link tool to join plinths into a channel");
		add("plinthworks.hint.plinth.1", "Does nothing until a sigil is slotted in.");
		add("plinthworks.hint.plinth.2", "Wears the block it was crafted from.");
		add("plinthworks.hint.plinth_core.1", "Craft with four of one block to make a plinth.");
		add("plinthworks.hint.plinth_core.2", "Stone, bricks, sandstone and similar blocks only.");
		add("plinthworks.hint.sigil_base.1", "Craft with one ingredient to make a sigil.");
		add("plinthworks.hint.sigil_base.2", "The ingredient picks the job.");
		add("plinthworks.hint.etching_base.1", "Craft with one ingredient to make an etching.");
		add("plinthworks.hint.etching_base.2", "The ingredient picks the stat: Rate, Move, Hold or Reach.");
		add("plinthworks.hint.seal_base.1", "Craft with one ingredient to make a seal.");
		add("plinthworks.hint.seal_base.2", "The ingredient picks how it matches.");
		add("plinthworks.link_tool.bind", "Use on a plinth to bind it.");
		add("plinthworks.link_tool.join", "Use on another to join its channel.");
		add("plinthworks.link_tool.clear", "Sneak-use clears. Name it in an anvil to name the channel.");
		add("plinthworks.sigil.import.desc", "Pulls from neighbouring blocks into its buffer");
		add("plinthworks.sigil.export.desc", "Pushes its buffer into neighbouring blocks");
		add("plinthworks.sigil.crafting.desc", "Crafts the shown item from nearby ingredients. Off by default: set crafting.enabled in the config.");
		add("plinthworks.sigil.void.desc", "Deletes what is in its own buffer. Never touches neighbouring containers.");
		add("plinthworks.sigil.breaker.desc", "Breaks blocks in its area. Drops go to nearby storage.");
		add("plinthworks.sigil.placer.desc", "Places blocks it pulls from nearby storage");
		add("plinthworks.sigil.planter.desc", "Plants seeds and saplings it pulls from nearby storage");
		add("plinthworks.sigil.harvester.desc", "Harvests ripe crops and replants them");
		add("plinthworks.sigil.magnet.desc", "Sweeps dropped items into nearby storage");
		add("plinthworks.etching.speed.desc", "Less waiting between runs");
		add("plinthworks.etching.capacity.desc", "Moves more each run");
		add("plinthworks.etching.storage.desc", "A bigger buffer");
		add("plinthworks.etching.range.desc", "Reaches further, both for its work area and for links");
		add("plinthworks.etching.tier", "Tier %s");
		add("plinthworks.seal.item.desc", "Matches the item, ignores components");
		add("plinthworks.seal.item_exact.desc", "Matches the item and every component");
		add("plinthworks.seal.mod.desc", "Matches everything from one mod");
		add("plinthworks.seal.tag.desc", "Matches everything in one tag");
		add("plinthworks.seal.component.desc", "Matches one chosen component");
		add("plinthworks.seal.whitelist", "Allow list (%s)");
		add("plinthworks.seal.blacklist", "Block list (%s)");
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
		add("gui.plinthworks.stat_essentia_move", "Move: %s points");
		add("gui.plinthworks.stat_essentia", "%s / %s points");
		add("gui.plinthworks.stat_xp", "%s XP (%s mB)");
		add("gui.plinthworks.fluid_empty", "Empty");
		add("gui.plinthworks.stat_range", "Reach: %s");
		add("gui.plinthworks.stat_channel", "%s (%s)");
		add("gui.plinthworks.seal_config", "Seal settings");
		add("gui.plinthworks.no_seal", "No seal in this slot");
		add("gui.plinthworks.toggle_mode", "Allow or block");
		add("gui.plinthworks.mode_whitelist", "Allow");
		add("gui.plinthworks.mode_blacklist", "Block");
		add("gui.plinthworks.clear_keys", "Clear list");
		add("gui.plinthworks.add_displayed", "Add the shown item");
		add("gui.plinthworks.whitelist", "Allow list (%s)");
		add("gui.plinthworks.blacklist", "Block list (%s)");
		add("gui.plinthworks.redstone.always", "Always");
		add("gui.plinthworks.redstone.pause", "Pause");
		add("gui.plinthworks.redstone.run", "Run");
		add("gui.plinthworks.redstone.always.tip", "Redstone: always runs");
		add("gui.plinthworks.redstone.pause.tip", "Redstone: pauses when powered");
		add("gui.plinthworks.redstone.run.tip", "Redstone: runs only when powered");
		add("gui.plinthworks.mode.tip", "How the channel shares work");
		add("gui.plinthworks.resource.tip", "What this plinth moves");
		add("gui.plinthworks.mode.tip.state", "Transfer: %s");
		add("gui.plinthworks.mode.tip.no_channel", "Transfer needs a channel");
		add("gui.plinthworks.mode.tip.name_one", "Name one on the channel screen");
		add("gui.plinthworks.resource.tip.state", "Moves: %s");
		add("gui.plinthworks.verb.idle", "Idle");
		for (SigilType type : SigilType.values()) {
			add("gui.plinthworks.verb." + type.name().toLowerCase(Locale.ROOT), title(type.name()));
		}
		add("gui.plinthworks.resource.item", "Items");
		add("gui.plinthworks.resource.energy", "Energy");
		add("gui.plinthworks.resource.fluid", "Fluid");
		add("gui.plinthworks.resource.xp", "XP");
		add("gui.plinthworks.resource.essentia", "Essentia");
		add("gui.plinthworks.transfer.priority", "Priority");
		add("gui.plinthworks.transfer.round_robin", "Round robin");
		add("gui.plinthworks.transfer.load_balanced", "Balanced");
		add("gui.plinthworks.channel_local", "Local");
		add("gui.plinthworks.click_for", "Click for %s");
		add("gui.plinthworks.network_count", "%s plinths");
		add("gui.plinthworks.network_count_one", "%s plinth");
		add("gui.plinthworks.val_ticks", "%st");
		add("gui.plinthworks.val_fe", "%s FE");
		add("gui.plinthworks.val_mb", "%s mB");
		add("gui.plinthworks.val_pt", "%s pt");
		add("gui.plinthworks.network_mode", "Mode");
		add("gui.plinthworks.network_mode_unnamed", "Name the channel to pick a mode");
		add("gui.plinthworks.network_overview", "Channel");
		add("gui.plinthworks.network_members", "On this channel");
		add("gui.plinthworks.network_empty", "Nothing linked yet.");
		add("gui.plinthworks.network_name", "Channel name");
		add("gui.plinthworks.network_rename", "Rename");
		add("message.plinthworks.skip.full.breaker", "The breaker skipped a block: nowhere to put the drops.");
		add("message.plinthworks.skip.full.harvester", "The harvester skipped a crop: nowhere to put the drops.");
		add("message.plinthworks.skip.veto.breaker", "The breaker skipped a block: a claim or another mod stopped it.");
		add("message.plinthworks.skip.veto.harvester", "The harvester skipped a crop: a claim or another mod stopped it.");
		add("message.plinthworks.skip.failed.placer", "The placer skipped a spot: the block would not go there.");
		add("message.plinthworks.skip.failed.planter", "The planter skipped a spot: the plant would not go there.");
		add("message.plinthworks.link_cleared", "Link cleared.");
		add("message.plinthworks.link_bound", "Bound the link tool to %s.");
		add("message.plinthworks.link_joined", "Linked to channel %s.");
		add("message.plinthworks.link_range", "That plinth is out of range. Links reach %s blocks, in one dimension.");
	}

	private void advancement(String id, String title, String description) {
		add("advancements.plinthworks." + id + ".title", title);
		add("advancements.plinthworks." + id + ".description", description);
	}

	private static String title(String value) {
		String lower = value.toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1).replace('_', ' ');
	}

	// named after the stat labels in the plinth screen
	private static String etchingStat(EtchingType type) {
		return switch (type) {
			case SPEED -> "Rate";
			case CAPACITY -> "Move";
			case STORAGE -> "Hold";
			case RANGE -> "Reach";
		};
	}

	private static String sealName(SealType type) {
		return switch (type) {
			case ITEM -> "Item Seal";
			case ITEM_EXACT -> "Exact Seal";
			case MOD -> "Mod Seal";
			case TAG -> "Tag Seal";
			case COMPONENT -> "Component Seal";
		};
	}
}
