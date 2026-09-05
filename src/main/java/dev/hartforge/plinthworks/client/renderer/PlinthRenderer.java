package dev.hartforge.plinthworks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hartforge.plinthworks.block.entity.PlinthBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.*;

public class PlinthRenderer implements BlockEntityRenderer<PlinthBlockEntity>
{
	public PlinthRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public void render(PlinthBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
			int light, int overlay) {
		Minecraft minecraft = Minecraft.getInstance();
		poseStack.pushPose();
		poseStack.translate(0.1245F, 0, 0.1245F);
		poseStack.scale(0.751F, 0.751F, 0.751F);
		minecraft.getBlockRenderer().renderSingleBlock(be.getBaseState(), poseStack, buffers, light, overlay);
		poseStack.popPose();

		ItemStack stack = be.getDisplayedItem();
		if (stack.isEmpty() || be.getLevel() == null) {
			return;
		}
		float l = be.getLevel().getGameTime() + partialTick;
		float bob = (float) Math.sin(l / 10.0F) * 0.05F;
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.12F + bob, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(l * 3.0F));
		poseStack.scale(0.5F, 0.5F, 0.5F);
		minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, poseStack,
				buffers, be.getLevel(), 0);
		poseStack.popPose();
	}
}
