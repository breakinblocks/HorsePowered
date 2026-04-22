package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class WorkingAreaRenderer {

    public static void renderIfActive(HPBlockEntityHorseBase blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        if (blockEntity.shouldShowHighlight()) {
            render(blockEntity, poseStack, bufferSource);
        }
    }

    public static void render(HPBlockEntityHorseBase blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        BlockPos blockPos = blockEntity.getBlockPos();
        List<Map.Entry<BlockPos, Boolean>> positions = blockEntity.getWorkingAreaPositions();

        if (positions.isEmpty()) {
            return;
        }

        renderFilledBoxes(positions, blockPos, poseStack);
        renderWireframes(positions, blockPos, poseStack, bufferSource);
    }

    // Uses raw Tesselator rather than RenderType so the faces stay additive/transparent — RenderType.translucent
    // would respect depth-sort and occlude against itself when boxes overlap.
    private static void renderFilledBoxes(List<Map.Entry<BlockPos, Boolean>> positions, BlockPos blockPos, PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Map.Entry<BlockPos, Boolean> entry : positions) {
            BlockPos pos = entry.getKey();
            boolean isClear = entry.getValue();

            float x = pos.getX() - blockPos.getX();
            float y = pos.getY() - blockPos.getY();
            float z = pos.getZ() - blockPos.getZ();

            int r = isClear ? 0 : 255;
            int g = isClear ? 255 : 0;
            int b = 0;
            int a = 25;

            renderFilledBox(buffer, matrix, x, y, z, x + 1, y + 1, z + 1, r, g, b, a);
        }

        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderWireframes(List<Map.Entry<BlockPos, Boolean>> positions, BlockPos blockPos,
                                          PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());

        for (Map.Entry<BlockPos, Boolean> entry : positions) {
            BlockPos pos = entry.getKey();
            boolean isClear = entry.getValue();

            double x = pos.getX() - blockPos.getX();
            double y = pos.getY() - blockPos.getY();
            double z = pos.getZ() - blockPos.getZ();

            float r = isClear ? 0.0f : 1.0f;
            float g = isClear ? 1.0f : 0.0f;

            AABB box = new AABB(x, y, z, x + 1, y + 1, z + 1);
            LevelRenderer.renderLineBox(poseStack, lineConsumer, box, r, g, 0.0f, 1.0f);
        }
    }

    private static void renderFilledBox(BufferBuilder buffer, Matrix4f matrix,
                                         float x1, float y1, float z1,
                                         float x2, float y2, float z2,
                                         int r, int g, int b, int a) {
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
    }
}
