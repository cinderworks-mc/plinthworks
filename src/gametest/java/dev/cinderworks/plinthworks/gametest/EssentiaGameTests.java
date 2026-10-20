package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.item.EtchingTier;
import dev.cinderworks.plinthworks.item.EtchingType;
import dev.cinderworks.plinthworks.item.SigilType;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import dev.cinderworks.plinthworks.logic.resource.Essentia;
import dev.cinderworks.plinthworks.logic.resource.EssentiaHandler;
import dev.cinderworks.plinthworks.logic.resource.EssentiaProvider;
import dev.cinderworks.plinthworks.logic.resource.EssentiaSupport;
import dev.cinderworks.plinthworks.registry.ModDataComponents;
import dev.cinderworks.plinthworks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// the essentia mode with a stand-in provider (glass blocks as jars) and with none at all. the provider is global,
// so each case runs in a batch that swaps it in and puts back whatever was there (thaumaturge, in a dev run with it)
@PrefixGameTestTemplate(false)
public class EssentiaGameTests
{
	private static final String FAKE = "essentia_fake";
	private static final String KEPT = "essentia_kept";
	private static final String NONE = "essentia_none";
	private static final BlockPos POS = new BlockPos(2, 2, 2);
	private static final BlockPos BELOW = POS.below();
	private static final int WAIT = 24;
	private static final ResourceLocation AER = ResourceLocation.fromNamespaceAndPath("fake", "aer");
	private static final ResourceLocation IGNIS = ResourceLocation.fromNamespaceAndPath("fake", "ignis");

	private static final Jars JARS = new Jars();
	private static EssentiaProvider before;

	@BeforeBatch(batch = FAKE)
	public static void fakeProvider(ServerLevel level) {
		swapIn(JARS);
	}

	@AfterBatch(batch = FAKE)
	public static void fakeProviderDone(ServerLevel level) {
		swapBack();
	}

	@BeforeBatch(batch = KEPT)
	public static void fluxOff(ServerLevel level) {
		swapIn(JARS);
		ModConfig.ESSENTIA_FLUX_ON_BREAK.set(false);
	}

	@AfterBatch(batch = KEPT)
	public static void fluxOffDone(ServerLevel level) {
		ModConfig.ESSENTIA_FLUX_ON_BREAK.set(true);
		swapBack();
	}

	@BeforeBatch(batch = NONE)
	public static void noProvider(ServerLevel level) {
		swapIn(null);
	}

	@AfterBatch(batch = NONE)
	public static void noProviderDone(ServerLevel level) {
		swapBack();
	}

	private static void swapIn(EssentiaProvider provider) {
		before = EssentiaSupport.provider();
		EssentiaSupport.register(provider);
	}

	private static void swapBack() {
		EssentiaSupport.register(before);
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = FAKE)
	public static void importDrainsTheJarUnderIt(GameTestHelper helper) {
		Jar jar = JARS.place(helper, BELOW, AER, 40);
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.IMPORT);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			expectBuffer(helper, be, AER, 40);
			Support.expectEquals(helper, 0, jar.amount, "jar");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = FAKE)
	public static void exportStopsAtAFullJar(GameTestHelper helper) {
		Jar jar = JARS.place(helper, BELOW, AER, 245);
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.EXPORT);
		be.essentia().insert(AER, 30, false);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, 250, jar.amount, "jar");
			expectBuffer(helper, be, AER, 25);
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = FAKE)
	public static void exportLeavesAJarOfAnotherAspectAlone(GameTestHelper helper) {
		Jar jar = JARS.place(helper, BELOW, IGNIS, 10);
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.EXPORT);
		be.essentia().insert(AER, 30, false);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, 10, jar.amount, "jar");
			Support.expectEquals(helper, IGNIS, jar.aspect, "jar aspect");
			expectBuffer(helper, be, AER, 30);
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = FAKE)
	public static void rangedPlinthReachesAJarItIsNotOn(GameTestHelper helper) {
		Jar jar = JARS.place(helper, POS.east(2), IGNIS, 30);
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.IMPORT);
		be.etchings().setStackInSlot(2, Support.etching(EtchingType.RANGE, EtchingTier.T1, 1));
		be.slotsChanged();
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expect(helper, be.upgrades().range() >= 2, "range " + be.upgrades().range());
			expectBuffer(helper, be, IGNIS, 30);
			Support.expectEquals(helper, 0, jar.amount, "jar");
		}).thenSucceed();
	}

	// an import plinth on one jar, an export plinth on another, same channel. checked every tick, not just at the end
	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = FAKE, timeoutTicks = 200)
	public static void linkedPairMovesJarToJarWithoutLosingAPoint(GameTestHelper helper) {
		BlockPos far = new BlockPos(10, 2, 10);
		Jar source = JARS.place(helper, BELOW, AER, 100);
		Jar dest = JARS.place(helper, far.below(), null, 0);
		PlinthBlockEntity near = essentiaPlinth(helper, POS, SigilType.IMPORT);
		PlinthBlockEntity other = essentiaPlinth(helper, far, SigilType.EXPORT);
		near.setChannel("gt_essentia_pair");
		other.setChannel("gt_essentia_pair");
		helper.onEachTick(() -> {
			int total = source.amount + dest.amount + near.essentia().amount() + other.essentia().amount();
			if (total != 100) {
				helper.fail("points in play went from 100 to " + total);
			}
		});
		helper.succeedWhen(() -> {
			Support.expectEquals(helper, 100, dest.amount, "far jar");
			Support.expectEquals(helper, AER, dest.aspect, "far jar aspect");
		});
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = FAKE)
	public static void voidSigilTurnsEssentiaIntoFlux(GameTestHelper helper) {
		Jar jar = JARS.place(helper, BELOW, AER, 40);
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.VOID);
		be.essentia().insert(IGNIS, 30, false);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, 0, be.essentia().amount(), "plinth buffer");
			Support.expectEquals(helper, 30, JARS.flux(helper.absolutePos(POS)), "flux at the plinth");
			Support.expectEquals(helper, 40, jar.amount, "jar under the void plinth");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = FAKE)
	public static void breakingAPlinthReleasesItsEssentia(GameTestHelper helper) {
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.IMPORT);
		be.essentia().insert(AER, 37, false);
		breakWithDrops(helper, POS);
		helper.startSequence().thenIdle(2).thenExecute(() -> {
			Support.expectEquals(helper, 37, JARS.flux(helper.absolutePos(POS)), "flux where the plinth stood");
			ItemStack dropped = droppedPlinth(helper);
			Support.expect(helper, !dropped.isEmpty(), "no plinth dropped");
			Support.expect(helper, !dropped.has(ModDataComponents.PLINTH_ESSENTIA.get()), "the dropped plinth still carries essentia");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = KEPT)
	public static void withFluxOffABrokenPlinthCarriesItsEssentia(GameTestHelper helper) {
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.IMPORT);
		be.essentia().insert(AER, 37, false);
		breakWithDrops(helper, POS);
		helper.startSequence().thenIdle(2).thenExecute(() -> {
			Support.expectEquals(helper, 0, JARS.flux(helper.absolutePos(POS)), "flux");
			expectCarriedAndPlacedBack(helper, AER, 37);
		}).thenSucceed();
	}

	// 0.2.5 wrote no essentia tag and only modes 0 to 3
	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = NONE)
	public static void oldPlinthLoadsAsBefore(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		CompoundTag tag = be.saveWithFullMetadata(Support.registries(helper));
		tag.remove("Essentia");
		tag.putInt("ResourceMode", ResourceMode.FLUID.ordinal());
		be.loadWithComponents(tag, Support.registries(helper));
		Support.expectEquals(helper, ResourceMode.FLUID, be.resourceMode(), "mode");
		Support.expectEquals(helper, 0, be.essentia().amount(), "essentia");
		be.cycleResourceMode();
		Support.expectEquals(helper, ResourceMode.XP, be.resourceMode(), "after fluid");
		be.cycleResourceMode();
		Support.expectEquals(helper, ResourceMode.ITEM, be.resourceMode(), "after xp");
		helper.succeed();
	}

	// a world saved with thaumaturge, opened without it: the mode and the points stay, nothing moves
	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = NONE)
	public static void essentiaPlinthIdlesAndKeepsItsPointsWithoutAProvider(GameTestHelper helper) {
		helper.setBlock(BELOW, Blocks.CHEST);
		Support.container(helper, BELOW).setItem(0, new ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 10));
		PlinthBlockEntity be = Support.plinth(helper, POS);
		Support.fit(be, SigilType.IMPORT);
		CompoundTag tag = be.saveWithFullMetadata(Support.registries(helper));
		tag.putInt("ResourceMode", ResourceMode.ESSENTIA.ordinal());
		CompoundTag essentia = new CompoundTag();
		essentia.putString("Aspect", "thaumaturge:aer");
		essentia.putInt("Amount", 37);
		tag.put("Essentia", essentia);
		be.loadWithComponents(tag, Support.registries(helper));
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> {
			Support.expectEquals(helper, ResourceMode.ESSENTIA, be.resourceMode(), "mode");
			Support.expectEquals(helper, 37, be.essentia().amount(), "essentia points");
			Support.expectEquals(helper, 10, Support.count(Support.container(helper, BELOW),
					net.minecraft.world.item.Items.COBBLESTONE), "the chest under it");
			Support.expectEquals(helper, 37, Support.reload(helper, be, POS).essentia().amount(), "points after another save");
			// clicking away is one way, the points stay in the buffer for when the mod comes back
			be.cycleResourceMode();
			Support.expectEquals(helper, ResourceMode.ITEM, be.resourceMode(), "after essentia");
			Support.expectEquals(helper, 37, be.essentia().amount(), "points after leaving the mode");
		}).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = NONE)
	public static void theCycleNeverOffersEssentiaWithoutAProvider(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		for (int i = 0; i < 8; i++) {
			be.cycleResourceMode();
			Support.expect(helper, be.resourceMode() != ResourceMode.ESSENTIA, "essentia offered on click " + (i + 1));
		}
		Support.expectEquals(helper, ResourceMode.ITEM, be.resourceMode(), "eight clicks is twice round");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = NONE)
	public static void withoutAProviderABrokenPlinthCarriesItsEssentia(GameTestHelper helper) {
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.IMPORT);
		be.essentia().insert(IGNIS, 37, false);
		breakWithDrops(helper, POS);
		helper.startSequence().thenIdle(2).thenExecute(() -> expectCarriedAndPlacedBack(helper, IGNIS, 37)).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE, batch = NONE)
	public static void withoutAProviderVoidDeletesNothing(GameTestHelper helper) {
		PlinthBlockEntity be = essentiaPlinth(helper, POS, SigilType.VOID);
		be.essentia().insert(AER, 30, false);
		helper.startSequence().thenIdle(WAIT).thenExecute(() -> expectBuffer(helper, be, AER, 30)).thenSucceed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void essentiaSurvivesSaveAndPacket(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		be.essentia().insert(AER, 37, false);
		for (PlinthBlockEntity copy : new PlinthBlockEntity[]{Support.reload(helper, be, POS), Support.viaPacket(helper, be, POS)}) {
			Support.expectEquals(helper, AER, copy.essentia().aspect(), "essentia aspect");
			Support.expectEquals(helper, 37, copy.essentia().amount(), "essentia points");
		}
		helper.succeed();
	}

	private static PlinthBlockEntity essentiaPlinth(GameTestHelper helper, BlockPos pos, SigilType sigil) {
		PlinthBlockEntity be = Support.plinth(helper, pos);
		Support.fit(be, sigil);
		CompoundTag tag = be.saveWithFullMetadata(Support.registries(helper));
		tag.putInt("ResourceMode", ResourceMode.ESSENTIA.ordinal());
		be.loadWithComponents(tag, Support.registries(helper));
		return be;
	}

	// helper.destroyBlock drops nothing, a player or an explosion would
	private static void breakWithDrops(GameTestHelper helper, BlockPos pos) {
		helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
	}

	private static void expectBuffer(GameTestHelper helper, PlinthBlockEntity be, ResourceLocation aspect, int amount) {
		Support.expectEquals(helper, amount, be.essentia().amount(), "plinth points");
		if (amount > 0) {
			Support.expectEquals(helper, aspect, be.essentia().aspect(), "plinth aspect");
		}
	}

	private static ItemStack droppedPlinth(GameTestHelper helper) {
		AABB around = new AABB(helper.absolutePos(POS)).inflate(2);
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, around).stream().map(ItemEntity::getItem)
				.filter(stack -> stack.is(ModItems.PLINTH.get())).findFirst().orElse(ItemStack.EMPTY);
	}

	private static void expectCarriedAndPlacedBack(GameTestHelper helper, ResourceLocation aspect, int amount) {
		ItemStack dropped = droppedPlinth(helper);
		Support.expect(helper, !dropped.isEmpty(), "no plinth dropped");
		Support.expectEquals(helper, new Essentia(aspect, amount), dropped.get(ModDataComponents.PLINTH_ESSENTIA.get()), "carried");
		PlinthBlockEntity placed = Support.plinth(helper, POS);
		placed.applyComponentsFromItemStack(dropped);
		expectBuffer(helper, placed, aspect, amount);
	}

	static class Jar implements EssentiaHandler
	{
		ResourceLocation aspect;
		int amount;

		Jar(ResourceLocation aspect, int amount) {
			this.aspect = amount > 0 ? aspect : null;
			this.amount = amount;
		}

		@Override
		public List<Essentia> contents() {
			return amount > 0 ? List.of(new Essentia(aspect, amount)) : List.of();
		}

		@Override
		public int insert(ResourceLocation id, int points, boolean simulate) {
			if (amount > 0 && !id.equals(aspect)) {
				return 0;
			}
			int moved = Math.max(0, Math.min(points, 250 - amount));
			if (!simulate && moved > 0) {
				aspect = id;
				amount += moved;
			}
			return moved;
		}

		@Override
		public int extract(ResourceLocation id, int points, boolean simulate) {
			if (amount <= 0 || !id.equals(aspect)) {
				return 0;
			}
			int moved = Math.min(points, amount);
			if (!simulate) {
				amount -= moved;
				if (amount == 0) {
					aspect = null;
				}
			}
			return moved;
		}
	}

	// glass blocks registered as jars, and a flux counter per position
	static class Jars implements EssentiaProvider
	{
		private final Map<BlockPos, Jar> jars = new ConcurrentHashMap<>();
		private final Map<BlockPos, Integer> flux = new ConcurrentHashMap<>();

		Jar place(GameTestHelper helper, BlockPos pos, ResourceLocation aspect, int amount) {
			helper.setBlock(pos, Blocks.GLASS);
			Jar jar = new Jar(aspect, amount);
			jars.put(helper.absolutePos(pos), jar);
			return jar;
		}

		int flux(BlockPos absolute) {
			return flux.getOrDefault(absolute, 0);
		}

		@Override
		public EssentiaHandler find(Level level, BlockPos pos, Direction side) {
			return level.getBlockState(pos).is(Blocks.GLASS) ? jars.get(pos) : null;
		}

		@Override
		public void release(Level level, BlockPos pos, ResourceLocation aspect, int points) {
			flux.merge(pos.immutable(), points, Integer::sum);
		}
	}
}
