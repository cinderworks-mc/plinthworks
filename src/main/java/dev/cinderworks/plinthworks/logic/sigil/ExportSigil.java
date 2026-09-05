package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.*;

public class ExportSigil implements SigilBehavior
{
	@Override
	public void run(PlinthBlockEntity be, UpgradeSet upgrades) {
		PlinthTick.push(be, upgrades);
	}
}
