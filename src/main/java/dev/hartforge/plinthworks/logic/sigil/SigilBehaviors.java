package dev.hartforge.plinthworks.logic.sigil;

import dev.hartforge.plinthworks.item.SigilType;

import java.util.Map;

public class SigilBehaviors
{
	private static final Map<SigilType, SigilBehavior> BEHAVIORS = Map.of(
			SigilType.IMPORT, new ImportSigil(),
			SigilType.EXPORT, new ExportSigil(),
			SigilType.CRAFTING, new CraftSigil(),
			SigilType.VOID, new VoidSigil());

	public static SigilBehavior get(SigilType type) {
		return BEHAVIORS.get(type);
	}
}
