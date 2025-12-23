package ru.timaaos;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import ru.timaaos.blocks.DashboardBlockEntity;
import ru.timaaos.blocks.DashboardBlockEntityRenderer;
import ru.timaaos.network.DashboardPackets;
import ru.timaaos.ui.DashboardScreen;

public class CreateDataAndPlotsClient implements ClientModInitializer {
	public class OpenDashboardScreenPacket {
		private final BlockPos pos;

		public OpenDashboardScreenPacket(BlockPos pos) {
			this.pos = pos;
		}

		public static void handle(OpenDashboardScreenPacket packet) {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.world == null) return;

			BlockEntity be = mc.world.getBlockEntity(packet.pos);
			if (be instanceof DashboardBlockEntity dashboard) {
				mc.setScreen(new DashboardScreen(dashboard));
			}
		}
	}

	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(CreateDataAndPlots.DASHBOARD_BLOCK_ENTITY, DashboardBlockEntityRenderer::new);
			ClientPlayNetworking.registerGlobalReceiver(
					DashboardPackets.OPEN_SCREEN,
					(client, handler, buf, responseSender) -> {

						BlockPos pos = buf.readBlockPos();

						client.execute(() -> {
							if (client.world.getBlockEntity(pos)
									instanceof DashboardBlockEntity be) {

								client.setScreen(new DashboardScreen(be));
							}
						});
					});
	}
}