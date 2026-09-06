package dev.hartforge.plinthworks.block;

import com.mojang.serialization.MapCodec;
import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.component.PlinthContents;
import dev.hartforge.plinthworks.menu.PlinthMenu;
import dev.hartforge.plinthworks.item.*;
import dev.hartforge.plinthworks.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import org.jetbrains.annotations.Nullable;

public class PlinthBlock extends BaseEntityBlock
{
	public static final MapCodec<PlinthBlock> CODEC = simpleCodec(PlinthBlock::new);
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

	public PlinthBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PlinthBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PLINTH.get(), PlinthBlockEntity::serverTick);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof PlinthBlockEntity plinth)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (stack.getItem() instanceof LinkToolItem) {
			return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		}
		if (stack.getItem() instanceof SigilItem || stack.getItem() instanceof EtchingItem
				|| stack.getItem() instanceof SealItem) {
			if (player instanceof ServerPlayer serverPlayer) {
				openMenu(serverPlayer, plinth, pos);
			}
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		if (player.isShiftKeyDown() && !plinth.getDisplayedItem().isEmpty()) {
			if (!level.isClientSide) {
				plinth.takeDisplayedItem(player);
			}
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		if (!stack.isEmpty() && plinth.getDisplayedItem().isEmpty()) {
			if (!level.isClientSide) {
				plinth.putDisplayedItem(player, stack);
			}
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof PlinthBlockEntity plinth)) {
			return InteractionResult.PASS;
		}
		if (!player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
			openMenu(serverPlayer, plinth, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	private static void openMenu(ServerPlayer player, PlinthBlockEntity plinth, BlockPos pos) {
		player.openMenu(new SimpleMenuProvider(
				(id, inventory, p) -> new PlinthMenu(id, inventory, plinth),
				Component.translatable("container.plinthworks.plinth")), buf -> {
			buf.writeBlockPos(pos);
			java.util.List<BlockPos> members = plinth.networkMembers();
			buf.writeVarInt(members.size());
			members.forEach(buf::writeBlockPos);
		});
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!(level.getBlockEntity(pos) instanceof PlinthBlockEntity plinth)) {
			return;
		}
		PlinthContents contents = stack.get(ModDataComponents.PLINTH_CONTENTS.get());
		if (contents != null) {
			plinth.setBaseState(BuiltInRegistries.BLOCK.get(contents.baseBlock()).defaultBlockState());
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
		if (level.getBlockEntity(pos) instanceof PlinthBlockEntity plinth) {
			stack.set(ModDataComponents.PLINTH_CONTENTS.get(),
					new PlinthContents(BuiltInRegistries.BLOCK.getKey(plinth.getBaseState().getBlock())));
		}
		return stack;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PlinthBlockEntity plinth) {
			plinth.dropStoredItems();
			plinth.setChannel("");
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}
}
