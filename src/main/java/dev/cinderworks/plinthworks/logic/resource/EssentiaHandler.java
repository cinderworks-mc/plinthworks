package dev.cinderworks.plinthworks.logic.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

// anything that holds essentia: a plinth buffer, or a vessel some other mod hands over through a provider.
// insert and extract are partial and return what moved (or would move, when simulating)
public interface EssentiaHandler
{
	List<Essentia> contents();

	int insert(ResourceLocation aspect, int points, boolean simulate);

	int extract(ResourceLocation aspect, int points, boolean simulate);
}
