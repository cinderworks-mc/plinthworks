package dev.cinderworks.plinthworks.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetChannelPayloadTest
{
	@Test
	void sanitizeNormalisesAndBounds() {
		assertEquals("my-chan_1", SetChannelPayload.sanitize("  My-Chan_1 "));
		assertEquals("abc", SetChannelPayload.sanitize("a b/cé"));
		assertEquals("", SetChannelPayload.sanitize("   !!! "));
		assertEquals(SetChannelPayload.MAX_LENGTH, SetChannelPayload.sanitize("x".repeat(500)).length());
	}
}
