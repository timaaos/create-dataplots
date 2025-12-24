package ru.timaaos.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class DashboardBlockEntityRenderer implements BlockEntityRenderer<DashboardBlockEntity> {
    private final BlockEntityRendererFactory.Context context;
    private static final int FULL_BRIGHT = 15728880;

    // CONFIGURATION: 0 = hide, 1 = attach to top (outside), 2 = attach to bottom (inside)
    private final int labelMode = 1;
    private final int itemMode = 2;

    public DashboardBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(DashboardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getWorld() == null) return;

        int[] heights = entity.getBarHeights();
        int[][] colors = entity.getBarColors();
        if (heights.length == 0) return;

        // --- 1. DYNAMIC SCALING ---
        int maxValue = 1;
        for (int v : heights) maxValue = Math.max(maxValue, v);

        float availableHeight = (float) entity.plotHeight;
        float totalDisplayWidth = (float) entity.plotWidth;
        float barWidth = totalDisplayWidth / heights.length;
        float xOffset = totalDisplayWidth / 2f;
        float barScaleY = availableHeight / (maxValue*1.1f);

        // UNIFORM SCALE: Shrink labels/items if the bar width is too narrow
        float elementScale = Math.min(1.0f, barWidth / 0.5f);
        elementScale = Math.max(elementScale, 0.35f);

        matrices.push();
        matrices.translate(0.5, 1.0, 0.5);

        // Rotation Logic
        BlockState state = entity.getCachedState();
        Direction facing = state.contains(Properties.HORIZONTAL_FACING) ? state.get(Properties.HORIZONTAL_FACING) : Direction.NORTH;
        float angle = switch (facing) {
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> 270;
            default -> 0;
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getGui());

        for (int i = 0; i < heights.length; i++) {
            float x0 = (i * barWidth) - xOffset;
            float x1 = x0 + barWidth;
            float centerX = (x0 + x1) / 2;

            float actualBarHeight = heights[i] * barScaleY;
            if (actualBarHeight < 0.05f && heights[i] > 0) actualBarHeight = 0.05f;

            // --- STEP 1: RENDER BAR ---
            renderBar(matrices, vertexConsumer, x0, x1, actualBarHeight, colors, i);

            // --- STEP 2: RENDER LABELS & ITEMS ---
            // Threshold for "short" bars to prevent overlap
            boolean isTooShort = actualBarHeight < 0.4f;

            // ITEM PLACEMENT
            if (itemMode != 0) {
                ItemStack stack = entity.getItemForLabel(i);
                boolean shouldBeAtTop = (itemMode == 1 || (itemMode == 2 && isTooShort));

                // Added 0.15f buffer to the item position
                float itemY = shouldBeAtTop ? actualBarHeight + (0.25f * elementScale) : (0.2f * elementScale);

                // Extra offset if text is also at the top to prevent item/text overlapping
                if (shouldBeAtTop && labelMode == 1) itemY += (0.2f * elementScale);

                renderItemLabel(matrices, stack, elementScale, centerX, itemY, -0.26f, vertexConsumers);
            }

            // TEXT PLACEMENT
            if (labelMode != 0) {
                String label = formatValue(heights[i]);
                boolean shouldBeAtTop = (labelMode == 1 || (labelMode == 2 && isTooShort));

                float textY = shouldBeAtTop ? actualBarHeight + 0.15f : 0.05f;
                if (!shouldBeAtTop && !isTooShort) textY = actualBarHeight - 0.15f;

                drawHeightText(matrices, label, elementScale, centerX, textY, -0.27f, vertexConsumers);
            }
        }

        matrices.pop();
    }

    private void renderBar(MatrixStack matrices, VertexConsumer vc, float x0, float x1, float y1, int[][] colors, int i) {
        float z0 = -0.25f;
        float z1 = 0.05f;
        int r = 255, g = 255, b = 255;
        if (i < colors.length) {
            r = colors[i][0]; g = colors[i][1]; b = colors[i][2];
        }

        Vector3f v0 = new Vector3f(x0, 0f, z0); Vector3f v1 = new Vector3f(x1, 0f, z0);
        Vector3f v2 = new Vector3f(x1, 0f, z1); Vector3f v3 = new Vector3f(x0, 0f, z1);
        Vector3f v4 = new Vector3f(x0, y1, z0); Vector3f v5 = new Vector3f(x1, y1, z0);
        Vector3f v6 = new Vector3f(x1, y1, z1); Vector3f v7 = new Vector3f(x0, y1, z1);

        drawQuad(matrices, vc, v0, v3, v7, v4, r, g, b, 255, FULL_BRIGHT, 0, 0, 0); // Left
        drawQuad(matrices, vc, v1, v0, v4, v5, r, g, b, 255, FULL_BRIGHT, 0, 0, 0); // Front
        drawQuad(matrices, vc, v2, v1, v5, v6, r, g, b, 255, FULL_BRIGHT, 0, 0, 0); // Right
        drawQuad(matrices, vc, v3, v2, v6, v7, r, g, b, 255, FULL_BRIGHT, 0, 0, 0); // Back
        drawQuad(matrices, vc, v4, v7, v6, v5, r, g, b, 255, FULL_BRIGHT, 0, 0, 0); // Top
    }

    private void renderItemLabel(MatrixStack matrices, ItemStack stack, float scale, float x, float y, float z, VertexConsumerProvider vertexConsumers) {
        if (stack.isEmpty()) return;
        matrices.push();
        matrices.translate(x, y, z);
        float s = 0.45f * scale;
        matrices.scale(s, s, s);
        this.context.getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, FULL_BRIGHT, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, null, 0);
        matrices.pop();
    }

    private void drawHeightText(MatrixStack matrices, String text, float scale, float x, float y, float z, VertexConsumerProvider vertexConsumers) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        matrices.push();
        matrices.translate(x, y, z);
        float s = -0.018f * scale; // Slightly smaller text for cleaner look
        matrices.scale(s, s, s);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float textX = -textRenderer.getWidth(text) * 0.5f;
        textRenderer.draw(text, textX, 0, 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, FULL_BRIGHT);
        matrices.pop();
    }

    private String formatValue(int value) {
        if (value >= 1000000) return String.format("%.1fM", value / 1000000.0);
        if (value >= 1000) return String.format("%.1fk", value / 1000.0);
        return String.valueOf(value);
    }

    private void drawQuad(MatrixStack matrices, VertexConsumer vc, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, int r, int g, int b, int a, int light, float nx, float ny, float nz) {
        MatrixStack.Entry entry = matrices.peek();
        vertex(entry, vc, v0, r, g, b, a, light, nx, ny, nz);
        vertex(entry, vc, v1, r, g, b, a, light, nx, ny, nz);
        vertex(entry, vc, v2, r, g, b, a, light, nx, ny, nz);
        vertex(entry, vc, v3, r, g, b, a, light, nx, ny, nz);
    }

    private void vertex(MatrixStack.Entry entry, VertexConsumer vc, Vector3f pos, int r, int g, int b, int a, int light, float nx, float ny, float nz) {
        vc.vertex(entry.getPositionMatrix(), pos.x, pos.y, pos.z)
                .color(r, g, b, a)
                .texture(0, 0)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry.getNormalMatrix(), nx, ny, nz)
                .next();
    }
}