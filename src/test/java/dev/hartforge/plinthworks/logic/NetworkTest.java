package dev.hartforge.plinthworks.logic;

import dev.hartforge.plinthworks.logic.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.LongUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class NetworkTest
{
	@Test
	void addRemoveAndCycle() {
		ChannelGraph graph = new ChannelGraph();
		graph.add("forge", 1);
		graph.add("forge", 2);
		assertEquals(2, graph.members("forge").size());
		assertEquals(TransferMode.ROUND_ROBIN, graph.cycleMode("forge"));
		graph.remove("forge", 1);
		assertEquals(List.of(2L), graph.members("forge"));
	}

	@Test
	void emptyChannelIsForgotten() {
		ChannelGraph graph = new ChannelGraph();
		graph.add("a", 5);
		graph.remove("a", 5);
		assertFalse(graph.names().contains("a"));
	}

	@Test
	void roundRobinRotatesSource() {
		ChannelGraph graph = new ChannelGraph();
		for (long pos = 0; pos < 3; pos++) {
			graph.add("c", pos);
		}
		graph.cycleMode("c");
		LongUnaryOperator noLoad = pos -> 0;
		assertEquals(List.of(1L, 2L), graph.ordered("c", 0, noLoad));
		assertEquals(List.of(2L, 1L), graph.ordered("c", 0, noLoad));
	}

	@Test
	void pruneDropsFlagged() {
		ChannelGraph graph = new ChannelGraph();
		graph.add("c", 1);
		graph.add("c", 2);
		assertTrue(graph.prune(pos -> pos == 1));
		assertEquals(List.of(2L), graph.members("c"));
	}
}
