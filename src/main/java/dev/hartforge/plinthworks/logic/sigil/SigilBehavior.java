package dev.hartforge.plinthworks.logic.sigil;

import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.logic.UpgradeSet;

public interface SigilBehavior
{
	void run(PlinthBlockEntity be, UpgradeSet upgrades);
}
