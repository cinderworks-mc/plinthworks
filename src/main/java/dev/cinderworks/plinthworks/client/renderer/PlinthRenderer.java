package dev.cinderworks.plinthworks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
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
		// the worn block shows on the step and the body, the baked model draws the trim over them
		// boxes in /16 units (x0,y0,z0 -> x1,y1,z1)
		renderBox(minecraft, be, poseStack, buffers, light, overlay, 1, 2, 1, 15, 4, 15);    // step
		renderBox(minecraft, be, poseStack, buffers, light, overlay, 2, 4, 2, 14, 11, 14);   // body

		ItemStack stack = be.getDisplayedItem();
		if (stack.isEmpty() || be.getLevel() == null) {
			return;
		}
		float l = be.getLevel().getGameTime() + partialTick;
		float bob = (float) Math.sin(l / 10.0F) * 0.03F;
		poseStack.pushPose();
		// sits in the bowl, just over the rim
		poseStack.translate(0.5F, 0.90F + bob, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(l * 3.0F));
		poseStack.scale(0.45F, 0.45F, 0.45F);
		minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, poseStack,
				buffers, be.getLevel(), 0);
		poseStack.popPose();
	}

	private static void renderBox(Minecraft minecraft, PlinthBlockEntity be, PoseStack poseStack,
			MultiBufferSource buffers, int light, int overlay,
			int x0, int y0, int z0, int x1, int y1, int z1) {
		poseStack.pushPose();
		poseStack.translate(x0 / 16.0F, y0 / 16.0F, z0 / 16.0F);
		poseStack.scale((x1 - x0) / 16.0F, (y1 - y0) / 16.0F, (z1 - z0) / 16.0F);
		minecraft.getBlockRenderer().renderSingleBlock(be.getBaseState(), poseStack, buffers, light, overlay);
		poseStack.popPose();
	}
}
