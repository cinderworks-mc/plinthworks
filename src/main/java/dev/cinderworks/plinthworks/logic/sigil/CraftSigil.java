package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.logic.*;

public class CraftSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		if (ModConfig.CRAFTING_ENABLED.get()) {
			PlinthTick.craft(be, upgrades);
		}
	}
}
