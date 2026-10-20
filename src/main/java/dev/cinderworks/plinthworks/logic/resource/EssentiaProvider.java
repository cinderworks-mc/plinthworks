package dev.cinderworks.plinthworks.logic.resource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

// what a magic mod plugs in to give plinths an essentia mode. registered through EssentiaSupport
public interface EssentiaProvider
{
	// a vessel at pos, or null. side is null for ranged scans and linked plinths
	EssentiaHandler find(Level level, BlockPos pos, Direction side);

	// points that leave the world (a void sigil, a broken plinth, a refund nobody takes back)
	void release(Level level, BlockPos pos, ResourceLocation aspect, int points);
}
