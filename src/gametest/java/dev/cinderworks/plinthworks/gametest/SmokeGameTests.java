package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class SmokeGameTests
{
	@GameTest(template = "empty", templateNamespace = "plinthworks")
	public static void plinthPlacesWithBlockEntity(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.PLINTH.get());
		if (!(helper.getBlockEntity(pos) instanceof PlinthBlockEntity)) {
			helper.fail("no plinth block entity", pos);
		}
		helper.succeed();
	}
}
