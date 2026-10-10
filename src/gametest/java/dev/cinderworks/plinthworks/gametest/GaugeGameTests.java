package dev.cinderworks.plinthworks.gametest;

import dev.cinderworks.plinthworks.block.entity.PlinthBlockEntity;
import dev.cinderworks.plinthworks.item.EtchingTier;
import dev.cinderworks.plinthworks.item.EtchingType;
import dev.cinderworks.plinthworks.menu.PlinthMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class GaugeGameTests
{
	private static final BlockPos POS = new BlockPos(2, 1, 2);

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void energyGaugeShowsFortyThousand(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		be.energy().receiveEnergy(40000, false);
		PlinthMenu client = synced(helper, be);
		Support.expectEquals(helper, 40000, client.energyStored(), "stored energy on the client");
		Support.expectEquals(helper, 40000, client.energyCapacity(), "energy capacity on the client");
		helper.succeed();
	}

	@GameTest(template = Support.TEMPLATE, templateNamespace = Support.NAMESPACE)
	public static void gaugesCarryTheWidestValues(GameTestHelper helper) {
		PlinthBlockEntity be = Support.plinth(helper, POS);
		for (int i = 0; i < 4; i++) {
			be.etchings().setStackInSlot(i, Support.etching(EtchingType.STORAGE, EtchingTier.T4, 1));
		}
		be.energy().receiveEnergy(200000, false);
		be.fluid().fill(new FluidStack(Fluids.WATER, 50000), IFluidHandler.FluidAction.EXECUTE);
		PlinthMenu client = synced(helper, be);
		Support.expectEquals(helper, 200000, client.energyStored(), "stored energy on the client");
		Support.expectEquals(helper, 320000, client.energyCapacity(), "energy capacity on the client");
		Support.expectEquals(helper, 50000, client.fluidStored(), "stored fluid on the client");
		Support.expectEquals(helper, 64000, client.fluidCapacity(), "fluid capacity on the client");
		helper.succeed();
	}

	// the server menu's data slots go over the wire as 16 bit shorts, the client menu only sees what the packet kept
	private static PlinthMenu synced(GameTestHelper helper, PlinthBlockEntity be) {
		ServerPlayer player = Support.player(helper);
		PlinthMenu server = new PlinthMenu(1, player.getInventory(), be);
		PlinthMenu client = new PlinthMenu(1, player.getInventory(), helper.absolutePos(POS));
		server.addSlotListener(new ContainerListener()
		{
			@Override
			public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack stack) {}

			@Override
			public void dataChanged(AbstractContainerMenu menu, int slot, int value) {
				FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
				ClientboundContainerSetDataPacket.STREAM_CODEC.encode(buf, new ClientboundContainerSetDataPacket(1, slot, value));
				ClientboundContainerSetDataPacket packet = ClientboundContainerSetDataPacket.STREAM_CODEC.decode(buf);
				client.setData(packet.getId(), packet.getValue());
			}
		});
		server.broadcastChanges();
		return client;
	}
}
