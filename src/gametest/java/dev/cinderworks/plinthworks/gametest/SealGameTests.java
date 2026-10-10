package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.component.MatchKey;
import dev.cinderworks.plinthworks.item.SealType;
import dev.cinderworks.plinthworks.item.SigilType;
import dev.cinderworks.plinthworks.logic.seal.SealMatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class SealGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void itemSealMatchesTheItem(GameTestHelper helper) {
		MatchKey cobble = new MatchKey.ItemKey(ResourceLocation.withDefaultNamespace("cobblestone"));
		PlinthBlockEntity be = withSeals(helper, Support.seal(SealType.ITEM, true, cobble));
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), true, "whitelisted item");
		expectAllows(helper, be, new ItemStack(Items.DIRT), false, "other item behind a whitelist");
		Support.expect(helper, !be.display().isItemValid(0, new ItemStack(Items.DIRT)), "the buffer took an item the whitelist refuses");
		Support.expect(helper, be.display().isItemValid(0, new ItemStack(Items.COBBLESTONE)), "the buffer refused the whitelisted item");
		be = withSeals(helper, Support.seal(SealType.ITEM, false, cobble));
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), false, "blacklisted item");
		expectAllows(helper, be, new ItemStack(Items.DIRT), true, "other item behind a blacklist");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void exactSealSeparatesComponents(GameTestHelper helper) {
		ItemStack plain = new ItemStack(Items.COBBLESTONE);
		ItemStack named = new ItemStack(Items.COBBLESTONE);
		named.set(DataComponents.CUSTOM_NAME, Component.literal("renamed"));
		MatchKey exact = new MatchKey.ComponentKey(ResourceLocation.fromNamespaceAndPath("plinthworks", "exact"),
				SealMatch.componentFingerprint(plain));
		PlinthBlockEntity be = withSeals(helper, Support.seal(SealType.ITEM_EXACT, true, exact));
		expectAllows(helper, be, plain, true, "stack with the same components");
		expectAllows(helper, be, named, false, "renamed stack behind an exact whitelist");
		expectAllows(helper, be, new ItemStack(Items.DIRT), false, "other item behind an exact whitelist");
		be = withSeals(helper, Support.seal(SealType.ITEM_EXACT, false, exact));
		expectAllows(helper, be, plain, false, "stack with the same components behind an exact blacklist");
		expectAllows(helper, be, named, true, "renamed stack behind an exact blacklist");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void modSealMatchesTheNamespace(GameTestHelper helper) {
		ItemStack own = Support.sigil(SigilType.MAGNET);
		PlinthBlockEntity be = withSeals(helper, Support.seal(SealType.MOD, true, new MatchKey.ModKey("minecraft")));
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), true, "vanilla item behind a minecraft whitelist");
		expectAllows(helper, be, own, false, "plinthworks item behind a minecraft whitelist");
		be = withSeals(helper, Support.seal(SealType.MOD, false, new MatchKey.ModKey("minecraft")));
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), false, "vanilla item behind a minecraft blacklist");
		expectAllows(helper, be, own, true, "plinthworks item behind a minecraft blacklist");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void tagSealMatchesTheTag(GameTestHelper helper) {
		MatchKey planks = new MatchKey.TagKey(ResourceLocation.withDefaultNamespace("planks"));
		PlinthBlockEntity be = withSeals(helper, Support.seal(SealType.TAG, true, planks));
		expectAllows(helper, be, new ItemStack(Items.OAK_PLANKS), true, "oak planks behind a planks whitelist");
		expectAllows(helper, be, new ItemStack(Items.BIRCH_PLANKS), true, "birch planks behind a planks whitelist");
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), false, "cobblestone behind a planks whitelist");
		be = withSeals(helper, Support.seal(SealType.TAG, false, planks));
		expectAllows(helper, be, new ItemStack(Items.OAK_PLANKS), false, "oak planks behind a planks blacklist");
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), true, "cobblestone behind a planks blacklist");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void componentSealMatchesTheValue(GameTestHelper helper) {
		ItemStack worn = new ItemStack(Items.DIAMOND_PICKAXE);
		worn.setDamageValue(5);
		ItemStack wornMore = new ItemStack(Items.DIAMOND_PICKAXE);
		wornMore.setDamageValue(6);
		MatchKey damage = new MatchKey.ComponentKey(ResourceLocation.withDefaultNamespace("damage"), "5");
		PlinthBlockEntity be = withSeals(helper, Support.seal(SealType.COMPONENT, true, damage));
		expectAllows(helper, be, worn, true, "pickaxe with the keyed damage");
		expectAllows(helper, be, wornMore, false, "pickaxe with other damage");
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), false, "item without the component");
		be = withSeals(helper, Support.seal(SealType.COMPONENT, false, damage));
		expectAllows(helper, be, worn, false, "pickaxe with the keyed damage behind a blacklist");
		expectAllows(helper, be, wornMore, true, "pickaxe with other damage behind a blacklist");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void sealBankCombinesWhitelistAndBlacklist(GameTestHelper helper) {
		MatchKey cobble = new MatchKey.ItemKey(ResourceLocation.withDefaultNamespace("cobblestone"));
		MatchKey dirt = new MatchKey.ItemKey(ResourceLocation.withDefaultNamespace("dirt"));
		PlinthBlockEntity be = withSeals(helper, Support.seal(SealType.ITEM, true, cobble, dirt),
				Support.seal(SealType.ITEM, false, dirt), Support.seal(SealType.MOD, true));
		expectAllows(helper, be, new ItemStack(Items.COBBLESTONE), true, "item only the whitelist names");
		expectAllows(helper, be, new ItemStack(Items.DIRT), false, "item the blacklist removes");
		expectAllows(helper, be, new ItemStack(Items.STONE), false, "item no whitelist names");
		be = withSeals(helper, Support.seal(SealType.MOD, true));
		expectAllows(helper, be, new ItemStack(Items.STONE), true, "item behind a seal with no keys");
		helper.succeed();
	}

	private static PlinthBlockEntity withSeals(GameTestHelper helper, ItemStack... seals) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		for (int i = 0; i < seals.length; i++) {
			be.seals().setStackInSlot(i, seals[i]);
		}
		return be;
	}

	private static void expectAllows(GameTestHelper helper, PlinthBlockEntity be, ItemStack stack, boolean allowed, String what) {
		Support.expectEquals(helper, allowed, be.matchesFilter(stack), what);
	}
}
