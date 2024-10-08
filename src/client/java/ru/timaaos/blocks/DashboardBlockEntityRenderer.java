package ru.timaaos.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.Font;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;

import java.awt.Color;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class DashboardBlockEntityRenderer implements BlockEntityRenderer<DashboardBlockEntity> {
    private final BlockEntityRendererFactory.Context context;

    /*private final BlockEntityRendererFactory.Context context;
        public DashboardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {this.context = ctx;}
        @Override
        public void render(DashboardBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
            matrices.push();
            double xCO = 0.0f;
            double zCO = 0.0f;
            switch(blockEntity.getWorld().getBlockState(blockEntity.getPos()).get(HorizontalFacingBlock.FACING)){
                case EAST:
                    xCO = 0.75f;
                    zCO = 0.5f;
                    break;
                case WEST:
                    xCO = 0.25f;
                    zCO = 0.5f;
                    break;
                case NORTH:
                    xCO = 0.5f;
                    zCO = 0.25f;
                    break;
                case SOUTH:
                    xCO = 0.5f;
                    zCO = 0.75f;
                    break;
            }
            double s = Math.sin((blockEntity.getWorld().getTime() + tickDelta)/20f);
            double height = Math.abs(s);
            double yOff = height/2;
            matrices.scale(0.1F, (float) height, 0.1f);
            matrices.translate(xCO,yOff,zCO);

            this.context.getItemRenderer().renderItem(new ItemStack(Items.BLUE_WOOL),ModelTransformationMode.NONE,255,0,matrices,vertexConsumers, this.context.getRenderDispatcher().world, 0);
            matrices.pop();

            /*matrices.push();
            matrices.translate(0f, 3f, 0f);
            matrices.scale(1F, 3- ((float) Math.abs(15*offset)), 1f);
            this.context.getItemRenderer().renderItem(new ItemStack(Items.GREEN_WOOL),ModelTransformationMode.FIXED,255,0,matrices,vertexConsumers, this.context.getRenderDispatcher().world, 0);
            matrices.pop();
        }*/
    public DashboardBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
        // Constructor with context can be useful for accessing other render components
    }

    @Override
    public void render(DashboardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        light = 15728880;
        int[] heights = entity.getBarHeights();
        int[][] colors = entity.getBarColors();
        int plotHeight = entity.plotHeight.getValue();
        float barWidth = (float) entity.plotWidth.getValue() / heights.length;
        float xOffset = (float) entity.plotWidth.getValue() / 2;
        int maxHeight = 0;
        for (int v:heights) {
            if(v > maxHeight){
                maxHeight = v;
            }
        }
        float heightMultiplier = (10f*plotHeight-10f/Math.max(1,1/barWidth/1.5f)) / maxHeight;
        MinecraftClient client = MinecraftClient.getInstance();
        matrices.push();
        matrices.translate(0.5, 1F, 0.5F);

        // Get the block's facing direction
        Direction facing = Direction.NORTH;
        try{
            facing = entity.getWorld().getBlockState(entity.getPos()).get(Properties.HORIZONTAL_FACING);
        }catch (Exception e){}

        switch (facing) {
            case WEST:
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                break;
            case EAST:
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
                break;
            case SOUTH:
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                break;
            case NORTH:
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
                break;
        }

        for (int i = 0; i < heights.length; i++) {
            float x0 = (i * barWidth) - xOffset;
            float x1 = x0 + barWidth;
            float z0 = -0.25f;
            float z1 = 0.25f;
            float y1 = 0.1f * heights[i]*heightMultiplier+1f/Math.max(1,1/barWidth/1.5f);

            int r,g,b;
            if(i < colors.length){
                r = colors[i][0];
                g = colors[i][1];
                b = colors[i][2];
            }else{
                r = 0;
                g = 0;
                b = 0;
            }


            Vector3f v0 = new Vector3f(x0, 0f, z0);
            Vector3f v1 = new Vector3f(x1, 0f, z0);
            Vector3f v2 = new Vector3f(x1, 0f, z1);
            Vector3f v3 = new Vector3f(x0, 0f, z1);
            Vector3f v4 = new Vector3f(x0, y1, z0);
            Vector3f v5 = new Vector3f(x1, y1, z0);
            Vector3f v6 = new Vector3f(x1, y1, z1);
            Vector3f v7 = new Vector3f(x0, y1, z1);
            if(y1 < 0.2f){
                drawHeightText(matrices, String.valueOf(heights[i]), Math.max(1,1/barWidth/1.5f), (x0 + x1) / 2, y1 + 0.1f, -0.26f, client, vertexConsumers);
            }else{
                drawHeightText(matrices, String.valueOf(heights[i]), Math.max(1,1/barWidth/1.5f), (x0 + x1) / 2, y1 - 0.05f, -0.26f, client, vertexConsumers);
            }

            drawQuad(matrices, vertexConsumer, v0, v3, v7, v4, new Color(r, g, b, 200), (int) (light/2)); // Left face
            drawQuad(matrices, vertexConsumer, v1, v0, v4, v5, new Color(r, g, b, 200), light); // Front face
            drawQuad(matrices, vertexConsumer, v2, v1, v5, v6, new Color(r, g, b, 200), (int) (light/2)); // Right face
            drawQuad(matrices, vertexConsumer, v3, v2, v6, v7, new Color(r, g, b, 200), light); // Back face
            drawQuad(matrices, vertexConsumer, v4, v7, v6, v5, new Color(r, g, b, 200), (int) (light/1.5)); // Top face
            ItemStack stack = entity.getItemForLabel(i);
            renderItemLabel(matrices, stack, Math.max(1,1/barWidth/1.5f), (x0 + x1) / 2, 0f, -0.26f, client, vertexConsumers, light);

        }

        matrices.pop();
    }

    private void drawQuad(MatrixStack matrices, VertexConsumer vertexConsumer, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Color color, int light) {
        MatrixStack.Entry entry = matrices.peek();
        vertex(entry, vertexConsumer, v0, color, light);
        vertex(entry, vertexConsumer, v1, color, light);
        vertex(entry, vertexConsumer, v2, color, light);
        vertex(entry, vertexConsumer, v3, color, light);
    }

    public void renderItemLabel(MatrixStack matrices, ItemStack stack, float sizemult, float x, float y, float z, MinecraftClient client, VertexConsumerProvider vertexConsumers, int light) {
        ItemRenderer itemRenderer = this.context.getItemRenderer();
        matrices.push();
        matrices.translate(x, y+(0.5f/sizemult)/2, z);
        //matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(1f/sizemult, 1f/sizemult, 1f/sizemult);
        itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, null , 0);
        matrices.pop();
    }



    private void drawHeightText(MatrixStack matrices, String text, float sizemult, float x, float y, float z, MinecraftClient client, VertexConsumerProvider vertexConsumers) {
        TextRenderer textRenderer = client.textRenderer;  // Get the current TextRenderer instance
        matrices.push();  // Push the current matrix stack to not disturb the original state

        // Translate to the correct location above the bar
        matrices.translate(x, y, z);
        //matrices.multiply(client.getEntityRenderDispatcher().getRotation());  // Correctly rotate towards the player
        matrices.scale(-0.025f/sizemult, -0.025f/sizemult, 0.025f/sizemult);  // Scale down the text and flip it to face the player

        // Prepare matrix and background color
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int backgroundColor = 0;  // No background color, use 0 for transparency

        // Calculate the width of the text for centering
        float textX = -textRenderer.getWidth(text) * 0.5f;  // Center text horizontally
        int color = 0xFFFFFF;  // White color for the text
        boolean shadow = false;  // Enable shadow
        int light = 15728880;  // Full bright light for the text

        // Render the text
        textRenderer.draw(text, textX, 0, color, shadow, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, backgroundColor, light);

        matrices.pop();  // Pop to revert to the original matrix stack state
    }

    private void vertex(MatrixStack.Entry entry, VertexConsumer vertexConsumer, Vector3f pos, Color color, int light) {
        vertexConsumer.vertex(entry.getPositionMatrix(), pos.x, pos.y, pos.z)
                .color(color.getRed(), color.getGreen(), color.getBlue(), 255)
                .texture(0, 0)
                .overlay(0, OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(1, 0, 0) // This needs to be calculated per face for correct lighting, simplified here
                .next();
    }
}
