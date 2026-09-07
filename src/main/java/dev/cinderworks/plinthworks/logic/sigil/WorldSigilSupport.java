package dev.cinderworks.plinthworks.logic.sigil;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.config.ModConfig;
import dev.cinderworks.plinthworks.logic.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;
import java.util.function.Predicate;

class WorldSigilSupport
{
	static Iterable<BlockPos> positions(PlinthBlockEntity be, int range) {
		BlockPos pos = be.getBlockPos();
		return BlockPos.betweenClosed(pos.offset(-range, -range, -range), pos.offset(range, range, range));
	}

	static List<IItemHandler> handlers(PlinthBlockEntity be, UpgradeSet upgrades) {
		return PlinthTick.itemHandlers(be, upgrades);
	}

	// placing verbs feed from the plinth's own slot first, then the network
	static List<IItemHandler> sources(PlinthBlockEntity be, UpgradeSet upgrades) {
		List<IItemHandler> list = new ArrayList<>();
		list.add(be.display());
		list.addAll(handlers(be, upgrades));
		return list;
	}

	static boolean filterAllows(PlinthBlockEntity be, UpgradeSet upgrades, ItemStack stack) {
		return !upgrades.filtered() || be.matchesFilter(stack);
	}

	static boolean needsFilter(UpgradeSet upgrades) {
		return ModConfig.REQUIRE_FILTER.get() && !upgrades.filtered();
	}

	static boolean blocked(BlockState state) {
		String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
		return ModConfig.BLOCK_BLOCKLIST.get().contains(id);
	}

	static boolean breakBlock(PlinthBlockEntity be, ServerLevel level, BlockPos pos, BlockState state,
			List<IItemHandler> dests) {
		FakePlayer player = PlinthFakePlayer.get(level, be.getBlockPos());
		if (ModConfig.FIRE_BLOCK_EVENTS.get()) {
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
			NeoForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return false;
			}
		}
		List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player,
				player.getMainHandItem());
		List<Insert> plan = plan(dests, drops);
		if (plan == null || !level.removeBlock(pos, false)) {
			return false;
		}
		for (Insert insert : plan) {
			insert.handler.insertItem(insert.slot, insert.stack, false);
		}
		return true;
	}

	static Source find(List<IItemHandler> handlers, Predicate<ItemStack> filter) {
		for (IItemHandler handler : handlers) {
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				ItemStack stack = handler.extractItem(slot, 1, true);
				if (!stack.isEmpty() && filter.test(stack)) {
					return new Source(handler, slot, stack);
				}
			}
		}
		return null;
	}

	static boolean place(ServerLevel level, FakePlayer player, Source source, BlockPos clicked, Direction face) {
		ItemStack one = source.stack.copyWithCount(1);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked), face, clicked, false);
		BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, one, hit);
		InteractionResult result = CommonHooks.onPlaceItemIntoWorld(context);
		if (!result.consumesAction()) {
			return false;
		}
		return !source.handler.extractItem(source.slot, 1, false).isEmpty();
	}

	static int insert(List<IItemHandler> handlers, ItemStack stack, int amount) {
		int moved = 0;
		for (IItemHandler handler : handlers) {
			for (int slot = 0; slot < handler.getSlots() && moved < amount; slot++) {
				ItemStack offered = stack.copyWithCount(Math.min(stack.getCount(), amount - moved));
				ItemStack left = handler.insertItem(slot, offered, false);
				int accepted = offered.getCount() - left.getCount();
				moved += accepted;
				stack.shrink(accepted);
				if (stack.isEmpty()) {
					return moved;
				}
			}
		}
		return moved;
	}

	private static List<Insert> plan(List<IItemHandler> handlers, List<ItemStack> drops) {
		List<Insert> plan = new ArrayList<>();
		IdentityHashMap<IItemHandler, Map<Integer, ItemStack>> reserved = new IdentityHashMap<>();
		for (ItemStack drop : drops) {
			ItemStack left = drop.copy();
			for (IItemHandler handler : handlers) {
				Map<Integer, ItemStack> slots = reserved.computeIfAbsent(handler, key -> new HashMap<>());
				for (int slot = 0; slot < handler.getSlots() && !left.isEmpty(); slot++) {
					ItemStack held = slots.getOrDefault(slot, ItemStack.EMPTY);
					if (!held.isEmpty() && !ItemStack.isSameItemSameComponents(held, left)) {
						continue;
					}
					int prior = held.getCount();
					int offeredCount = Math.min(left.getCount() + prior, left.getMaxStackSize());
					ItemStack offered = left.copyWithCount(offeredCount);
					ItemStack remainder = handler.insertItem(slot, offered, true);
					int accepted = Math.min(left.getCount(), offeredCount - remainder.getCount() - prior);
					if (accepted <= 0) {
						continue;
					}
					ItemStack stack = left.copyWithCount(accepted);
					plan.add(new Insert(handler, slot, stack));
					if (held.isEmpty()) {
						held = stack.copy();
						slots.put(slot, held);
					} else {
						held.grow(accepted);
					}
					left.shrink(accepted);
				}
				if (left.isEmpty()) {
					break;
				}
			}
			if (!left.isEmpty()) {
				return null;
			}
		}
		return plan;
	}

	record Source(IItemHandler handler, int slot, ItemStack stack) {}

	private record Insert(IItemHandler handler, int slot, ItemStack stack) {}
}
