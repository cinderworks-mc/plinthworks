package dev.hartforge.plinthworks.logic.sigil;

import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.config.ModConfig;
import dev.hartforge.plinthworks.logic.*;

public class CraftSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		if (ModConfig.CRAFTING_ENABLED.get()) {
			PlinthTick.craft(be, upgrades);
		}
	}
}
