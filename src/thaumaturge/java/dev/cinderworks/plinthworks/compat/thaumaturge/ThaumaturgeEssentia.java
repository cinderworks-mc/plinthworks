package dev.cinderworks.plinthworks.compat.thaumaturge;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import dev.cinderworks.plinthworks.logic.resource.Essentia;
import dev.cinderworks.plinthworks.logic.resource.EssentiaHandler;
import dev.cinderworks.plinthworks.logic.resource.EssentiaProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

class ThaumaturgeEssentia implements EssentiaProvider
{
	// jars only open on their top face. a plinth beside a jar, or one reaching it with a range etching, still goes
	// in through the top. anything else that answers on the asked side (an alembic, say) is fine too
	@Override
	public EssentiaHandler find(Level level, BlockPos pos, Direction side) {
		IEssentiaStorage storage = level.getCapability(EssentiaCapabilities.STORAGE, pos, Direction.UP);
		if (storage == null && side != null) {
			storage = level.getCapability(EssentiaCapabilities.STORAGE, pos, side);
		}
		return storage == null ? null : new Storage(storage, level);
	}

	// one point, one flux, the same rate thaumaturge uses when a jar is poured out
	@Override
	public void release(Level level, BlockPos pos, ResourceLocation aspect, int points) {
		AuraHelper.addFlux(level, pos, points);
	}

	private record Storage(IEssentiaStorage storage, Level level) implements EssentiaHandler
	{
		@Override
		public List<Essentia> contents() {
			List<Essentia> held = new ArrayList<>();
			for (AspectInstance entry : storage.contents().entries()) {
				entry.aspect().unwrapKey().ifPresent(key -> held.add(new Essentia(key.location(), entry.amount())));
			}
			return held;
		}

		@Override
		public int insert(ResourceLocation aspect, int points, boolean simulate) {
			Holder<IAspect> holder = resolve(aspect);
			return holder == null ? 0 : storage.insert(holder, points, simulate);
		}

		@Override
		public int extract(ResourceLocation aspect, int points, boolean simulate) {
			Holder<IAspect> holder = resolve(aspect);
			return holder == null ? 0 : storage.extract(holder, points, simulate);
		}

		// an aspect a datapack took away resolves to nothing, and nothing moves
		private Holder<IAspect> resolve(ResourceLocation aspect) {
			return Aspects.resolve(level, ResourceKey.create(IAspect.REGISTRY_KEY, aspect));
		}
	}
}
