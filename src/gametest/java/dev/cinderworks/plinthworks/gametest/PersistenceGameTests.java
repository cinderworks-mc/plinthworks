package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.component.MatchKey;
import dev.cinderworks.plinthworks.component.PlinthContents;
import dev.cinderworks.plinthworks.component.PlinthLoadout;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.RedstoneMode;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import dev.cinderworks.plinthworks.logic.network.PlinthNetwork;
import dev.cinderworks.plinthworks.logic.network.TransferMode;
import dev.cinderworks.plinthworks.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class PersistenceGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void fullPlinthSurvivesSaveAndReload(GameTestHelper helper) {
		PlinthBlockEntity be = stocked(helper);
		sameState(helper, be, Support.reload(helper, be, POS), "after nbt reload");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void updatePacketCarriesTheSameState(GameTestHelper helper) {
		PlinthBlockEntity be = stocked(helper);
		sameState(helper, be, Support.viaPacket(helper, be, POS), "after update packet");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void wornBlockSurvivesReloadAndItemForm(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		BlockState worn = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Z);
		be.setBaseState(worn);
		Support.expectEquals(helper, worn, Support.reload(helper, be, POS).getBaseState(), "worn block after nbt reload");
		Support.expectEquals(helper, worn, Support.viaPacket(helper, be, POS).getBaseState(), "worn block after update packet");
		be.sigil().setStackInSlot(0, Support.sigil(SigilType.MAGNET));
		DataComponentMap components = be.collectComponents();
		PlinthContents contents = components.get(ModDataComponents.PLINTH_CONTENTS.get());
		Support.expect(helper, contents != null, "no plinth contents component on the item form");
		Support.expectEquals(helper, BuiltInRegistries.BLOCK.getKey(Blocks.OAK_LOG), contents.baseBlock(), "worn block on the item form");
		PlinthLoadout loadout = components.get(ModDataComponents.PLINTH_LOADOUT.get());
		Support.expect(helper, loadout != null && loadout.sigil().is(Support.sigil(SigilType.MAGNET).getItem()), "sigil missing from the item form");
		PlinthBlockEntity placed = new PlinthBlockEntity(helper.absolutePos(POS), be.getBlockState());
		placed.applyComponents(components, DataComponentPatch.EMPTY);
		Support.expectEquals(helper, Blocks.OAK_LOG, placed.getBaseState().getBlock(), "worn block after placing the item");
		Support.expectStack(helper, Support.sigil(SigilType.MAGNET), placed.sigil().getStackInSlot(0), "sigil after placing the item");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void corruptModesLoadInRange(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		int[] stored = {-1, 4, 7, Integer.MIN_VALUE, Integer.MAX_VALUE};
		RedstoneMode[] expected = {RedstoneMode.RUN_WHEN_POWERED, RedstoneMode.PAUSE_WHEN_POWERED, RedstoneMode.PAUSE_WHEN_POWERED,
				RedstoneMode.PAUSE_WHEN_POWERED, RedstoneMode.PAUSE_WHEN_POWERED};
		for (int i = 0; i < stored.length; i++) {
			CompoundTag tag = be.saveWithFullMetadata(Support.registries(helper));
			tag.putInt("RedstoneMode", stored[i]);
			tag.putInt("ResourceMode", stored[i]);
			PlinthBlockEntity loaded = new PlinthBlockEntity(helper.absolutePos(POS), be.getBlockState());
			loaded.loadWithComponents(tag, Support.registries(helper));
			Support.expectEquals(helper, expected[i], loaded.redstoneMode(), "redstone mode loaded from " + stored[i]);
			Support.expectEquals(helper, ResourceMode.fromOrdinal(stored[i]), loaded.resourceMode(), "resource mode loaded from " + stored[i]);
		}
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void savedDataCarriesADataVersion(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		CompoundTag tag = be.saveWithoutMetadata(Support.registries(helper));
		Support.expect(helper, tag.contains("DataVersion"), "plinth save has no DataVersion");
		Support.expectEquals(helper, 1, tag.getInt("DataVersion"), "plinth data version");
		PlinthNetwork network = new PlinthNetwork();
		network.add("gt_version", helper.absolutePos(POS));
		network.cycleMode("gt_version");
		CompoundTag channels = network.save(new CompoundTag(), Support.registries(helper));
		Support.expect(helper, channels.contains("DataVersion"), "channel file has no DataVersion");
		Support.expectEquals(helper, 1, channels.getInt("DataVersion"), "channel data version");
		PlinthNetwork loaded = PlinthNetwork.load(channels, Support.registries(helper));
		Support.expectEquals(helper, java.util.List.of(helper.absolutePos(POS)), loaded.channel("gt_version").members(), "channel members after reload");
		Support.expectEquals(helper, TransferMode.PRIORITY.next(), loaded.channel("gt_version").mode(), "channel mode after reload");
		helper.succeed();
	}

	private static PlinthBlockEntity stocked(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		be.sigil().setStackInSlot(0, Support.sigil(SigilType.EXPORT));
		be.etchings().setStackInSlot(0, Support.etching(EtchingType.STORAGE, EtchingTier.T3, 2));
		be.etchings().setStackInSlot(2, Support.etching(EtchingType.RANGE, EtchingTier.T1, 1));
		be.seals().setStackInSlot(0, Support.seal(SealType.ITEM, false, new MatchKey.ItemKey(ResourceLocation.withDefaultNamespace("dirt"))));
		be.seals().setStackInSlot(2, Support.seal(SealType.MOD, true, new MatchKey.ModKey("minecraft")));
		be.display().insertItem(0, new ItemStack(Items.COBBLESTONE, 40), false);
		be.setBaseState(Blocks.DEEPSLATE_BRICKS.defaultBlockState());
		be.setChannel("gt_stocked");
		be.cycleRedstoneMode();
		be.cycleResourceMode();
		be.cycleResourceMode();
		be.energy().receiveEnergy(12345, false);
		be.fluid().fill(new FluidStack(Fluids.WATER, 1500), IFluidHandler.FluidAction.EXECUTE);
		return be;
	}

	private static void sameState(GameTestHelper helper, PlinthBlockEntity expected, PlinthBlockEntity actual, String when) {
		Support.expectHandler(helper, expected.sigil(), actual.sigil(), "sigil " + when);
		Support.expectHandler(helper, expected.etchings(), actual.etchings(), "etchings " + when);
		Support.expectHandler(helper, expected.seals(), actual.seals(), "seals " + when);
		Support.expectHandler(helper, expected.display(), actual.display(), "display " + when);
		Support.expectEquals(helper, expected.getBaseState(), actual.getBaseState(), "worn block " + when);
		Support.expectEquals(helper, expected.channel(), actual.channel(), "channel " + when);
		Support.expectEquals(helper, RedstoneMode.PAUSE_WHEN_POWERED, actual.redstoneMode(), "redstone mode " + when);
		Support.expectEquals(helper, ResourceMode.FLUID, actual.resourceMode(), "resource mode " + when);
		Support.expectEquals(helper, 12345, actual.energy().getEnergyStored(), "energy " + when);
		Support.expectEquals(helper, 1500, actual.fluid().getFluidAmount(), "fluid " + when);
		Support.expectEquals(helper, expected.upgrades(), actual.upgrades(), "upgrades " + when);
	}
}
