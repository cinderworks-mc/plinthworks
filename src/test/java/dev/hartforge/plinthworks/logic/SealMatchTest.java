package dev.hartforge.plinthworks.logic;

import dev.hartforge.plinthworks.logic.seal.SealMatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SealMatchTest
{
	@Test
	void whitelistOrAndBlacklistVeto() {
		assertTrue(SealMatch.bankAllowsResults(List.of(
				new SealMatch.SealResult(true, false),
				new SealMatch.SealResult(true, true),
				new SealMatch.SealResult(false, true))));
		assertFalse(SealMatch.bankAllowsResults(List.of(
				new SealMatch.SealResult(true, true),
				new SealMatch.SealResult(false, false))));
	}

	// real-key filtering can't run here - SealConfig/MatchKey static init drags in
	// net.minecraft off the test classpath - so just lock the empty edge
	@Test
	void keyedDropsEmptyBank() {
		assertTrue(SealMatch.keyed(List.of()).isEmpty());
	}
}
