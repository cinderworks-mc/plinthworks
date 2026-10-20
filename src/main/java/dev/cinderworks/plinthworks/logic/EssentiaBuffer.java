package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.logic.resource.Essentia;
import dev.cinderworks.plinthworks.logic.resource.EssentiaHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// one aspect at a time, whole points, the same rule a jar follows
public class EssentiaBuffer implements EssentiaHandler
{
	private final Runnable onChanged;
	private ResourceLocation aspect;
	private int amount;
	private int capacity;

	public EssentiaBuffer(int capacity, Runnable onChanged) {
		this.capacity = capacity;
		this.onChanged = onChanged;
	}

	public ResourceLocation aspect() {
		return aspect;
	}

	public int amount() {
		return amount;
	}

	public int capacity() {
		return capacity;
	}

	// a storage etching coming out never deletes points, the buffer just takes nothing until it drains
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public List<Essentia> contents() {
		return amount > 0 ? List.of(new Essentia(aspect, amount)) : List.of();
	}

	@Override
	public int insert(ResourceLocation id, int points, boolean simulate) {
		if (points <= 0 || (amount > 0 && !id.equals(aspect))) {
			return 0;
		}
		int moved = Math.min(points, capacity - amount);
		if (moved <= 0) {
			return 0;
		}
		if (!simulate) {
			aspect = id;
			amount += moved;
			onChanged.run();
		}
		return moved;
	}

	@Override
	public int extract(ResourceLocation id, int points, boolean simulate) {
		if (points <= 0 || amount <= 0 || !id.equals(aspect)) {
			return 0;
		}
		int moved = Math.min(points, amount);
		if (!simulate) {
			amount -= moved;
			if (amount == 0) {
				aspect = null;
			}
			onChanged.run();
		}
		return moved;
	}

	public Essentia drain() {
		Essentia held = amount > 0 ? new Essentia(aspect, amount) : null;
		if (held != null) {
			aspect = null;
			amount = 0;
			onChanged.run();
		}
		return held;
	}

	// puts back what a dropped plinth carried, past the capacity if it has to
	public void restore(Essentia held) {
		aspect = held == null || held.amount() <= 0 ? null : held.aspect();
		amount = aspect == null ? 0 : held.amount();
		onChanged.run();
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		if (aspect != null && amount > 0) {
			tag.putString("Aspect", aspect.toString());
			tag.putInt("Amount", amount);
		}
		return tag;
	}

	public void load(CompoundTag tag) {
		aspect = ResourceLocation.tryParse(tag.getString("Aspect"));
		amount = aspect == null ? 0 : Math.max(0, tag.getInt("Amount"));
		if (amount == 0) {
			aspect = null;
		}
	}
}
