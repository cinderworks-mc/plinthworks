package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.logic.FakeEssentia.Jar;
import dev.cinderworks.plinthworks.logic.resource.EssentiaHandler;
import dev.cinderworks.plinthworks.logic.resource.EssentiaResourceType;
import dev.cinderworks.plinthworks.logic.resource.EssentiaResourceType.Vessel;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static dev.cinderworks.plinthworks.logic.FakeEssentia.AER;
import static dev.cinderworks.plinthworks.logic.FakeEssentia.IGNIS;
import static org.junit.jupiter.api.Assertions.*;

class EssentiaMoveTest
{
	private final FakeEssentia provider = new FakeEssentia();
	private final EssentiaResourceType type = new EssentiaResourceType(provider);

	private static Vessel at(EssentiaHandler handler) {
		return new Vessel(handler, null, BlockPos.ZERO);
	}

	@Test
	void movesUpToTheAmountAndNoMore() {
		Jar jar = new Jar(AER, 40, 250);
		EssentiaBuffer plinth = new EssentiaBuffer(64, () -> {});
		assertEquals(16, type.move(at(jar), at(plinth), 16, a -> true));
		assertEquals(24, jar.amount);
		assertEquals(16, plinth.amount());
		assertEquals(AER, plinth.aspect());
	}

	@Test
	void fullOrEmptyEndsMoveNothing() {
		EssentiaBuffer full = new EssentiaBuffer(10, () -> {});
		full.insert(AER, 10, false);
		Jar jar = new Jar(AER, 40, 250);
		assertEquals(0, type.move(at(jar), at(full), 16, a -> true));
		assertEquals(40, jar.amount);

		Jar empty = new Jar(null, 0, 250);
		EssentiaBuffer plinth = new EssentiaBuffer(64, () -> {});
		assertEquals(0, type.move(at(empty), at(plinth), 16, a -> true));
		assertNull(plinth.aspect());
		assertEquals(0, provider.released());
	}

	@Test
	void wrongAspectAndLabelsAreRefused() {
		EssentiaBuffer plinth = new EssentiaBuffer(64, () -> {});
		plinth.insert(AER, 30, false);
		Jar ignis = new Jar(IGNIS, 10, 250);
		assertEquals(0, type.move(at(plinth), at(ignis), 16, a -> true));
		assertEquals(30, plinth.amount());
		assertEquals(10, ignis.amount);

		Jar labelled = new Jar(null, 0, 250);
		labelled.label = IGNIS;
		assertEquals(0, type.move(at(plinth), at(labelled), 16, a -> true));
		assertEquals(30, plinth.amount());

		assertEquals(0, type.move(at(ignis), at(plinth), 16, a -> true));
		assertEquals(0, provider.released());
	}

	@Test
	void filterIsAskedPerAspect() {
		Jar jar = new Jar(AER, 40, 250);
		EssentiaBuffer plinth = new EssentiaBuffer(64, () -> {});
		assertEquals(0, type.move(at(jar), at(plinth), 16, IGNIS::equals));
		assertEquals(40, jar.amount);
	}

	@Test
	void aDestinationThatTakesLessThanItPromisedRefundsTheSource() {
		Jar source = new Jar(AER, 40, 250);
		Jar dest = new Jar(null, 0, 250);
		dest.realInsertCap = 3;
		assertEquals(3, type.move(at(source), at(dest), 10, a -> true));
		assertEquals(37, source.amount);
		assertEquals(3, dest.amount);
		assertEquals(0, provider.released());
	}

	@Test
	void pointsNobodyTakesBackAreReleasedNotDeleted() {
		Jar source = new Jar(AER, 40, 250);
		source.refuseInserts = true;
		Jar dest = new Jar(null, 0, 250);
		dest.realInsertCap = 3;
		assertEquals(3, type.move(at(source), at(dest), 10, a -> true));
		assertEquals(30, source.amount);
		assertEquals(7, provider.released.get(AER));
	}

	@Test
	void voidReleasesWhatItTakesAndOnlyFromItsOwnBuffer() {
		EssentiaBuffer plinth = new EssentiaBuffer(64, () -> {});
		plinth.insert(IGNIS, 30, false);
		assertEquals(16, type.discard(at(plinth), 16, a -> true));
		assertEquals(14, plinth.amount());
		assertEquals(16, provider.released.get(IGNIS));
		assertEquals(14, type.discard(at(plinth), 16, a -> true));
		assertEquals(30, provider.released.get(IGNIS));
		assertNull(plinth.aspect());
	}

	// random moves between jars and buffers that fail in every way the fakes can: the total never changes
	@Test
	void pointsAreNeverMadeOrLost() {
		Random random = new Random(20261020L);
		for (int round = 0; round < 200; round++) {
			FakeEssentia counting = new FakeEssentia();
			EssentiaResourceType moving = new EssentiaResourceType(counting);
			EssentiaHandler[] vessels = {new Jar(AER, random.nextInt(251), 250), new Jar(IGNIS, random.nextInt(251), 250),
					new Jar(null, 0, 250), new EssentiaBuffer(64, () -> {}), new EssentiaBuffer(1 + random.nextInt(64), () -> {})};
			int start = total(vessels);
			for (int step = 0; step < 50; step++) {
				for (EssentiaHandler v : vessels) {
					if (v instanceof Jar jar) {
						jar.realInsertCap = random.nextInt(4) == 0 ? random.nextInt(5) : Integer.MAX_VALUE;
						jar.refuseInserts = random.nextInt(6) == 0;
					}
				}
				EssentiaHandler from = vessels[random.nextInt(vessels.length)];
				EssentiaHandler to = vessels[random.nextInt(vessels.length)];
				if (random.nextInt(10) == 0) {
					moving.discard(at(from), 1 + random.nextInt(16), a -> true);
				} else if (from != to) {
					int moved = moving.move(at(from), at(to), 1 + random.nextInt(16), a -> true);
					assertTrue(moved >= 0 && moved <= 16);
				}
				assertEquals(start, total(vessels) + counting.released(), "round " + round + " step " + step);
			}
		}
	}

	private static int total(EssentiaHandler... vessels) {
		int sum = 0;
		for (EssentiaHandler v : vessels) {
			sum += v.contents().stream().mapToInt(e -> e.amount()).sum();
		}
		return sum;
	}
}
