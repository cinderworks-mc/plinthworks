package dev.cinderworks.plinthworks.logic;

import dev.cinderworks.plinthworks.logic.seal.SealMatch;
import dev.cinderworks.plinthworks.logic.seal.SealMatch.SealResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// bank inputs are post-inversion: SealResult.match is "this seal allows the item".
// an unconfigured whitelist allows nothing (closed), an unconfigured blacklist blocks
// nothing so it allows everything (open).
class SealConfigTest
{
	@Test
	void emptyWhitelistIsClosed() {
		assertFalse(SealMatch.bankAllowsResults(List.of(new SealResult(true, false))));
	}

	@Test
	void emptyBlacklistIsOpen() {
		assertTrue(SealMatch.bankAllowsResults(List.of(new SealResult(false, true))));
	}

	@Test
	void noSealsAllowsEverything() {
		assertTrue(SealMatch.bankAllowsResults(List.of()));
	}

	@Test
	void multipleWhitelistsAnyMatchWins() {
		assertTrue(SealMatch.bankAllowsResults(List.of(
				new SealResult(true, false),
				new SealResult(true, true))));
	}
}
