package dev.hartforge.plinthworks.logic.sigil;

import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.logic.*;

public class ImportSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		PlinthTick.pull(be, upgrades);
	}
}
