package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.logic.resource.Essentia;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static dev.cinderworks.plinthworks.logic.FakeEssentia.AER;
import static dev.cinderworks.plinthworks.logic.FakeEssentia.IGNIS;
import static org.junit.jupiter.api.Assertions.*;

class EssentiaBufferTest
{
	@Test
	void holdsOneAspectUpToCapacity() {
		int[] changes = {0};
		EssentiaBuffer buffer = new EssentiaBuffer(10, () -> changes[0]++);
		assertEquals(6, buffer.insert(AER, 6, false));
		assertEquals(0, buffer.insert(IGNIS, 1, true));
		assertEquals(4, buffer.insert(AER, 9, true));
		assertEquals(6, buffer.amount());
		assertEquals(4, buffer.insert(AER, 9, false));
		assertEquals(0, buffer.insert(AER, 1, false));
		assertEquals(0, buffer.insert(AER, -3, false));
		assertEquals(2, changes[0]);
	}

	@Test
	void extractIsPartialAndEmptyingForgetsTheAspect() {
		EssentiaBuffer buffer = new EssentiaBuffer(10, () -> {});
		buffer.insert(AER, 3, false);
		assertEquals(0, buffer.extract(IGNIS, 3, false));
		assertEquals(3, buffer.extract(AER, 8, true));
		assertEquals(3, buffer.amount());
		assertEquals(3, buffer.extract(AER, 8, false));
		assertNull(buffer.aspect());
		assertTrue(buffer.contents().isEmpty());
		assertEquals(2, buffer.insert(IGNIS, 2, false));
	}

	@Test
	void smallerCapacityKeepsWhatIsAlreadyThere() {
		EssentiaBuffer buffer = new EssentiaBuffer(64, () -> {});
		buffer.insert(AER, 50, false);
		buffer.setCapacity(10);
		assertEquals(50, buffer.amount());
		assertEquals(0, buffer.insert(AER, 1, true));
		assertEquals(50, buffer.extract(AER, 64, false));
	}

	@Test
	void drainTakesEverythingAndRestorePutsItBack() {
		EssentiaBuffer buffer = new EssentiaBuffer(64, () -> {});
		assertNull(buffer.drain());
		buffer.insert(IGNIS, 37, false);
		Essentia held = buffer.drain();
		assertEquals(new Essentia(IGNIS, 37), held);
		assertEquals(0, buffer.amount());
		EssentiaBuffer placed = new EssentiaBuffer(16, () -> {});
		placed.restore(held);
		// back past a smaller capacity rather than lose points
		assertEquals(37, placed.amount());
		assertEquals(IGNIS, placed.aspect());
	}

	@Test
	void saveRoundTripsAndOldOrBrokenTagsReadEmpty() {
		EssentiaBuffer buffer = new EssentiaBuffer(64, () -> {});
		buffer.insert(IGNIS, 37, false);
		EssentiaBuffer loaded = new EssentiaBuffer(64, () -> {});
		loaded.load(buffer.save());
		assertEquals(IGNIS, loaded.aspect());
		assertEquals(37, loaded.amount());
		// a 0.2.5 save has no Essentia tag at all, getCompound hands back an empty one
		loaded.load(new CompoundTag());
		assertNull(loaded.aspect());
		assertEquals(0, loaded.amount());
		assertTrue(new EssentiaBuffer(64, () -> {}).save().isEmpty());
		CompoundTag negative = new CompoundTag();
		negative.putString("Aspect", "thaumaturge:aer");
		negative.putInt("Amount", -5);
		loaded.load(negative);
		assertNull(loaded.aspect());
		assertEquals(0, loaded.amount());
		CompoundTag junk = new CompoundTag();
		junk.putString("Aspect", "Not An Id!");
		junk.putInt("Amount", 5);
		loaded.load(junk);
		assertNull(loaded.aspect());
		assertEquals(0, loaded.amount());
	}
}
