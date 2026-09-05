package dev.hartforge.plinthworks.item;

import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import dev.hartforge.plinthworks.component.LinkBinding;
import dev.hartforge.plinthworks.logic.network.PlinthNetwork;
import dev.hartforge.plinthworks.registry.ModDataComponents;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Optional;

public class LinkToolItem extends Item
{
	public LinkToolItem() {
		super(new Properties().stacksTo(1).component(ModDataComponents.LINK_BINDING.get(), LinkBinding.EMPTY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !(context.getLevel().getBlockEntity(context.getClickedPos())
				instanceof PlinthBlockEntity plinth)) {
			return InteractionResult.PASS;
		}
		ItemStack tool = context.getItemInHand();
		if (player.isShiftKeyDown()) {
			if (!context.getLevel().isClientSide) {
				tool.set(ModDataComponents.LINK_BINDING.get(), LinkBinding.EMPTY);
				player.displayClientMessage(Component.translatable("message.plinthworks.link_cleared"), true);
			}
			return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
		}
		if (!(context.getLevel() instanceof ServerLevel level)) {
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = context.getClickedPos();
		LinkBinding binding = tool.getOrDefault(ModDataComponents.LINK_BINDING.get(), LinkBinding.EMPTY);
		if (binding.anchor().isEmpty()) {
			String channel = tool.has(DataComponents.CUSTOM_NAME)
					? channelName(tool.getHoverName().getString()) : defaultName(pos);
			tool.set(ModDataComponents.LINK_BINDING.get(),
					new LinkBinding(channel, Optional.of(GlobalPos.of(level.dimension(), pos))));
			PlinthNetwork.get(level).add(channel, pos);
			plinth.setChannel(channel);
			player.displayClientMessage(Component.translatable("message.plinthworks.link_bound", channel), true);
			return InteractionResult.SUCCESS;
		}

		GlobalPos anchor = binding.anchor().get();
		if (!anchor.dimension().equals(level.dimension())
				|| Math.sqrt(anchor.pos().distSqr(pos)) > plinth.upgrades().range()) {
			player.displayClientMessage(Component.translatable("message.plinthworks.link_range"), true);
			return InteractionResult.FAIL;
		}
		PlinthNetwork.get(level).add(binding.channel(), pos);
		plinth.setChannel(binding.channel());
		player.displayClientMessage(Component.translatable("message.plinthworks.link_joined", binding.channel()), true);
		return InteractionResult.SUCCESS;
	}

	private static String defaultName(BlockPos pos) {
		return "channel_" + Long.toUnsignedString(pos.asLong(), 36);
	}

	public static String channelName(String name) {
		String value = name.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
		return value.isBlank() ? "channel" : value.substring(0, Math.min(value.length(), 32));
	}
}
