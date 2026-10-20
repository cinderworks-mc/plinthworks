package dev.cinderworks.plinthworks.block;

import com.mojang.serialization.MapCodec;
import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.component.PlinthContents;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import dev.cinderworks.plinthworks.item.*;
import dev.cinderworks.plinthworks.logic.PlinthVisual;
import dev.cinderworks.plinthworks.logic.ResourceMode;
import dev.cinderworks.plinthworks.registry.*;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import org.jetbrains.annotations.Nullable;

public class PlinthBlock extends BaseEntityBlock
{
	public static final MapCodec<PlinthBlock> CODEC = simpleCodec(PlinthBlock::new);
	// only the model reads these: glow, resource glyph and how many etching bars are lit
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, ResourceMode.values().length - 1);
	public static final IntegerProperty GAUGE = IntegerProperty.create("gauge", 0, PlinthVisual.GAUGE_MAX);
	// foot, step, body, cap with its tray posts, the coal mound and the two flanking blocks
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(0, 0, 0, 16, 2, 16), Block.box(1, 2, 1, 15, 4, 15), Block.box(2, 4, 2, 14, 11, 14),
			Block.box(0, 11, 0, 16, 13, 16), Block.box(5, 13, 0, 7, 15, 2), Block.box(9, 13, 0, 11, 15, 2),
			Block.box(5, 13, 14, 7, 15, 16), Block.box(9, 13, 14, 11, 15, 16),
			Block.box(5, 13, 3, 11, 15, 13), Block.box(6, 15, 5, 10, 17, 11),
			Block.box(0, 13, 5, 5, 16, 11), Block.box(11, 13, 5, 16, 16, 11));

	public PlinthBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(MODE, 0).setValue(GAUGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE, MODE, GAUGE);
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
		// the trim is a baked model, the worn block body is drawn by the block entity renderer
		return RenderShape.MODEL;
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
		if (plinth.resourceMode() == ResourceMode.ITEM && player.isShiftKeyDown()
				&& !plinth.getDisplayedItem().isEmpty()) {
			if (!level.isClientSide) {
				plinth.takeDisplayedItem(player);
			}
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		if (plinth.resourceMode() == ResourceMode.ITEM && !stack.isEmpty() && plinth.getDisplayedItem().isEmpty()) {
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

	public static void openMenu(ServerPlayer player, PlinthBlockEntity plinth, BlockPos pos) {
		player.openMenu(new SimpleMenuProvider(
				(id, inventory, p) -> new PlinthMenu(id, inventory, plinth),
				Component.translatable("container.plinthworks.plinth")), buf -> {
			buf.writeBlockPos(pos);
			java.util.List<BlockPos> members = plinth.networkMembers();
			buf.writeVarInt(members.size());
			members.forEach(buf::writeBlockPos);
		});
	}

	public static void openSealMenu(ServerPlayer player, PlinthBlockEntity plinth, BlockPos pos, int sealIndex) {
		player.openMenu(new SimpleMenuProvider(
				(id, inventory, p) -> new dev.cinderworks.plinthworks.menu.SealMenu(id, inventory, plinth, sealIndex),
				Component.translatable("gui.plinthworks.seal_config")), buf -> {
			buf.writeBlockPos(pos);
			buf.writeVarInt(sealIndex);
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
			plinth.releaseEssentia();
			plinth.dropStoredItems();
			plinth.setChannel("");
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}
}
