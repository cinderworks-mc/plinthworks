package dev.cinderworks.plinthworks.gametest;

import com.mojang.authlib.GameProfile;
import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.component.MatchKey;
import dev.cinderworks.plinthworks.component.SealConfig;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.registry.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

final class Support
{
	static final String TEMPLATE = "empty";
	static final String NAMESPACE = "plinthworks";

	private Support() {}

	static PlinthBlockEntity plinth(GameTestHelper helper, BlockPos pos) {
		helper.setBlock(pos, ModBlocks.PLINTH.get());
		BlockEntity be = helper.getBlockEntity(pos);
		if (!(be instanceof PlinthBlockEntity plinth)) {
			helper.fail("no plinth block entity", pos);
			throw new IllegalStateException();
		}
		return plinth;
	}

	static ItemStack sigil(SigilType type) {
		return new ItemStack(ModItems.SIGILS.get(type).get());
	}

	static ItemStack etching(EtchingType type, EtchingTier tier, int count) {
		return new ItemStack(ModItems.ETCHINGS.get(type).get(tier).get(), count);
	}

	static ItemStack seal(SealType type, boolean whitelist, MatchKey... keys) {
		ItemStack stack = new ItemStack(ModItems.SEALS.get(type).get());
		stack.set(ModDataComponents.SEAL_CONFIG.get(), new SealConfig(whitelist, java.util.List.of(keys)));
		return stack;
	}

	// shortest interval and the widest throughput, so transfers finish in a few ticks
	static void quicken(PlinthBlockEntity be) {
		be.etchings().setStackInSlot(0, etching(EtchingType.SPEED, EtchingTier.T4, 2));
		be.etchings().setStackInSlot(1, etching(EtchingType.CAPACITY, EtchingTier.T4, 2));
	}

	static void fit(PlinthBlockEntity be, SigilType sigil) {
		be.sigil().setStackInSlot(0, sigil(sigil));
		quicken(be);
	}

	static Container container(GameTestHelper helper, BlockPos pos) {
		if (!(helper.getBlockEntity(pos) instanceof Container container)) {
			helper.fail("not a container", pos);
			throw new IllegalStateException();
		}
		return container;
	}

	static int count(Container container, Item item) {
		int total = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			if (container.getItem(i).is(item)) {
				total += container.getItem(i).getCount();
			}
		}
		return total;
	}

	static ServerPlayer player(GameTestHelper helper) {
		return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "[plinthworks-test]"));
	}

	static HolderLookup.Provider registries(GameTestHelper helper) {
		return helper.getLevel().registryAccess();
	}

	static int menuSlot(AbstractContainerMenu menu, Container container, int index) {
		for (Slot slot : menu.slots) {
			if (slot.container == container && slot.getContainerSlot() == index) {
				return slot.index;
			}
		}
		throw new IllegalStateException("no menu slot for " + index);
	}

	// what the chunk loader would build from the saved block entity
	static PlinthBlockEntity reload(GameTestHelper helper, PlinthBlockEntity be, BlockPos pos) {
		CompoundTag tag = be.saveWithFullMetadata(registries(helper));
		BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(pos), be.getBlockState(), tag, registries(helper));
		if (!(loaded instanceof PlinthBlockEntity plinth)) {
			helper.fail("saved plinth did not load back", pos);
			throw new IllegalStateException();
		}
		return plinth;
	}

	// what a client builds from the update packet, through the real packet codec
	static PlinthBlockEntity viaPacket(GameTestHelper helper, PlinthBlockEntity be, BlockPos pos) {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.STREAM_CODEC.encode(buf, be.getUpdatePacket());
		var packet = net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.STREAM_CODEC.decode(buf);
		PlinthBlockEntity client = new PlinthBlockEntity(helper.absolutePos(pos), be.getBlockState());
		client.onDataPacket(null, packet, registries(helper));
		return client;
	}

	static void expect(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			helper.fail(message);
		}
	}

	static void expectEquals(GameTestHelper helper, Object expected, Object actual, String what) {
		if (!expected.equals(actual)) {
			helper.fail(what + ": expected " + expected + " but was " + actual);
		}
	}

	static void expectStack(GameTestHelper helper, ItemStack expected, ItemStack actual, String what) {
		if (!ItemStack.matches(expected, actual)) {
			helper.fail(what + ": expected " + expected + " but was " + actual);
		}
	}

	static void expectHandler(GameTestHelper helper, ItemStackHandler expected, ItemStackHandler actual, String what) {
		expectEquals(helper, expected.getSlots(), actual.getSlots(), what + " slots");
		for (int i = 0; i < expected.getSlots(); i++) {
			expectStack(helper, expected.getStackInSlot(i), actual.getStackInSlot(i), what + " slot " + i);
		}
	}
}
