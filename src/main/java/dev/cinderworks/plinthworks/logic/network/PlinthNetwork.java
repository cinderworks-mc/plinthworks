package dev.cinderworks.plinthworks.logic.network;

import dev.cinderworks.plinthworks.registry.ModBlocks;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class PlinthNetwork extends SavedData
{
	private static final String FILE_NAME = "plinthworks_channels";
	private static final long PRUNE_INTERVAL = 200;
	private final ChannelGraph graph = new ChannelGraph();
	private long lastPrune = Long.MIN_VALUE;

	public PlinthNetwork() {}

	public static PlinthNetwork get(ServerLevel level) {
		PlinthNetwork data = level.getDataStorage().computeIfAbsent(
				new Factory<>(PlinthNetwork::new, PlinthNetwork::load), FILE_NAME);
		long now = level.getGameTime();
		if (now - data.lastPrune >= PRUNE_INTERVAL) {
			data.lastPrune = now;
			data.prune(level);
		}
		return data;
	}

	public void add(String channel, BlockPos pos) {
		if (graph.add(channel, pos.asLong())) {
			setDirty();
		}
	}

	public void remove(String channel, BlockPos pos) {
		if (graph.remove(channel, pos.asLong())) {
			setDirty();
		}
	}

	public NetworkChannel channel(String name) {
		return new NetworkChannel(name, unpack(graph.members(name)), graph.mode(name));
	}

	public List<BlockPos> orderedMembers(String name, BlockPos source, ServerLevel level) {
		List<Long> ordered = graph.ordered(name, source.asLong(), pos -> load(level, BlockPos.of(pos)));
		if (graph.mode(name) == TransferMode.ROUND_ROBIN) {
			setDirty();
		}
		return unpack(ordered);
	}

	public TransferMode cycleMode(String channel) {
		TransferMode mode = graph.cycleMode(channel);
		setDirty();
		return mode;
	}

	private static long load(ServerLevel level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity be) {
			return be.getDisplayedItem().getCount();
		}
		return Integer.MAX_VALUE;
	}

	private void prune(ServerLevel level) {
		if (graph.prune(pos -> level.hasChunkAt(BlockPos.of(pos))
				&& !level.getBlockState(BlockPos.of(pos)).is(ModBlocks.PLINTH.get()))) {
			setDirty();
		}
	}

	private static List<BlockPos> unpack(List<Long> packed) {
		List<BlockPos> out = new ArrayList<>(packed.size());
		for (long value : packed) {
			out.add(BlockPos.of(value));
		}
		return out;
	}

	public static PlinthNetwork load(CompoundTag tag, HolderLookup.Provider registries) {
		PlinthNetwork data = new PlinthNetwork();
		ListTag list = tag.getList("Channels", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag channel = list.getCompound(i);
			List<Long> members = new ArrayList<>();
			for (long value : channel.getLongArray("Members")) {
				members.add(value);
			}
			data.graph.restore(channel.getString("Name"), members,
					readMode(channel.getString("Mode")), channel.getInt("Cursor"));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		for (String name : graph.names()) {
			CompoundTag channel = new CompoundTag();
			channel.putString("Name", name);
			channel.putString("Mode", graph.mode(name).name());
			channel.putInt("Cursor", graph.cursor(name));
			channel.putLongArray("Members", graph.members(name).stream().mapToLong(Long::longValue).toArray());
			list.add(channel);
		}
		tag.put("Channels", list);
		return tag;
	}

	private static TransferMode readMode(String name) {
		return Arrays.stream(TransferMode.values()).filter(mode -> mode.name().equals(name))
				.findFirst().orElse(TransferMode.PRIORITY);
	}
}
