package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.logic.UpgradeSet;

public interface SigilBehavior
{
	void run(PlinthBlockEntity be, UpgradeSet upgrades);
}
