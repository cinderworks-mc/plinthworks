package dev.cinderworks.plinthworks.logic.network;

import java.util.*;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;

// the channel bookkeeping, split out of PlinthNetwork so it can be tested without
// a level. positions are packed longs (BlockPos.asLong) - the SavedData wrapper packs
// and unpacks.
public class ChannelGraph
{
	private final Map<String, Channel> channels = new LinkedHashMap<>();

	public boolean add(String channel, long pos) {
		return channels.computeIfAbsent(channel, name -> new Channel()).members.add(pos);
	}

	public boolean remove(String channel, long pos) {
		Channel state = channels.get(channel);
		if (state == null || !state.members.remove(pos)) {
			return false;
		}
		if (state.members.isEmpty()) {
			channels.remove(channel);
		}
		return true;
	}

	public List<Long> members(String channel) {
		Channel state = channels.get(channel);
		return state == null ? List.of() : List.copyOf(state.members);
	}

	public TransferMode mode(String channel) {
		Channel state = channels.get(channel);
		return state == null ? TransferMode.PRIORITY : state.mode;
	}

	public TransferMode cycleMode(String channel) {
		Channel state = channels.get(channel);
		if (state == null) {
			return TransferMode.PRIORITY;
		}
		state.mode = state.mode.next();
		return state.mode;
	}

	public List<Long> ordered(String channel, long source, LongUnaryOperator load) {
		Channel state = channels.get(channel);
		if (state == null) {
			return List.of();
		}
		ArrayList<Long> members = new ArrayList<>(state.members);
		members.remove(source);
		if (state.mode == TransferMode.ROUND_ROBIN && !members.isEmpty()) {
			Collections.rotate(members, -(state.cursor++ % members.size()));
		} else if (state.mode == TransferMode.LOAD_BALANCED) {
			members.sort(Comparator.comparingLong(load::applyAsLong));
		}
		return members;
	}

	public boolean prune(LongPredicate drop) {
		boolean changed = false;
		for (Iterator<Channel> it = channels.values().iterator(); it.hasNext();) {
			Channel state = it.next();
			changed |= state.members.removeIf(drop::test);
			if (state.members.isEmpty()) {
				it.remove();
				changed = true;
			}
		}
		return changed;
	}

	public Set<String> names() {
		return channels.keySet();
	}

	public int cursor(String channel) {
		Channel state = channels.get(channel);
		return state == null ? 0 : state.cursor;
	}

	public void restore(String channel, Collection<Long> members, TransferMode mode, int cursor) {
		Channel state = channels.computeIfAbsent(channel, name -> new Channel());
		state.members.addAll(members);
		state.mode = mode;
		state.cursor = cursor;
	}

	private static class Channel
	{
		private final Set<Long> members = new LinkedHashSet<>();
		private TransferMode mode = TransferMode.PRIORITY;
		private int cursor;
	}
}
