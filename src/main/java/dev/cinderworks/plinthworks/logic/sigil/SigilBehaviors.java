package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.item.SigilType;

import java.util.Map;

public class SigilBehaviors
{
	private static final Map<SigilType, SigilBehavior> BEHAVIORS = Map.ofEntries(
			Map.entry(SigilType.IMPORT, new ImportSigil()),
			Map.entry(SigilType.EXPORT, new ExportSigil()),
			Map.entry(SigilType.CRAFTING, new CraftSigil()),
			Map.entry(SigilType.VOID, new VoidSigil()),
			Map.entry(SigilType.BREAKER, new BreakerSigil()),
			Map.entry(SigilType.PLACER, new PlacerSigil()),
			Map.entry(SigilType.PLANTER, new PlanterSigil()),
			Map.entry(SigilType.HARVESTER, new HarvesterSigil()),
			Map.entry(SigilType.MAGNET, new MagnetSigil()));

	public static SigilBehavior get(SigilType type) {
		return BEHAVIORS.get(type);
	}
}
