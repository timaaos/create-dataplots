package ru.timaaos;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import ru.timaaos.blocks.DashboardBlockEntityRenderer;

public class CreateDataAndPlotsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(CreateDataAndPlots.DASHBOARD_BLOCK_ENTITY, DashboardBlockEntityRenderer::new);

	}
}