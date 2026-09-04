package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.WorkerHost;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared utility for rendering the working area highlight for horse-powered blocks.
 * Displays green boxes for clear areas and red boxes for obstructed areas.
 */
public class WorkingAreaRenderer {

    /**
     * Renders the working area highlight if the block entity should show it.
     *
     * @param blockEntity The horse-powered block entity
     * @param poseStack The pose stack for rendering
     * @param bufferSource The buffer source for rendering
     */
    public static void renderIfActive(WorkerHost blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        if (blockEntity.getVirtualWorker().shouldShowHighlight()) {
            render(blockEntity, poseStack, bufferSource);
        }
    }

    /**
     * Renders the working area highlight.
     *
     * @param blockEntity The horse-powered block entity
     * @param poseStack The pose stack for rendering
     * @param bufferSource The buffer source for rendering
     */
    public static void render(WorkerHost blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        BlockPos blockPos = blockEntity.getBlockPos();
        List<Map.Entry<BlockPos, Boolean>> positions = blockEntity.getVirtualWorker().getWorkingAreaPositions();

        if (positions.isEmpty()) {
            return;
        }

        // Render filled boxes
        renderFilledBoxes(positions, blockPos, poseStack);

        // Render wireframe outlines
        renderWireframes(positions, blockPos, poseStack, bufferSource);
    }

    private static void renderFilledBoxes(List<Map.Entry<BlockPos, Boolean>> positions, BlockPos blockPos, PoseStack poseStack) {
        // Build a set of all clear positions so we can skip internal faces
        Set<BlockPos> clearPositions = new HashSet<>();
        for (Map.Entry<BlockPos, Boolean> entry : positions) {
            if (entry.getValue()) {
                clearPositions.add(entry.getKey());
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Map.Entry<BlockPos, Boolean> entry : positions) {
            BlockPos pos = entry.getKey();
            boolean isClear = entry.getValue();

            float x = pos.getX() - blockPos.getX();
            float y = pos.getY() - blockPos.getY();
            float z = pos.getZ() - blockPos.getZ();

            if (isClear) {
                // Only render faces on the outer boundary of the clear area
                renderBoundaryFaces(buffer, matrix, pos, clearPositions, x, y, z,
                        50, 200, 50, 25);
            } else {
                // Obstructed blocks render as individual highlighted boxes
                renderFullBox(buffer, matrix,
                        x + 0.02f, y + 0.02f, z + 0.02f,
                        x + 0.98f, y + 0.98f, z + 0.98f,
                        255, 40, 40, 50);
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Renders only the faces of a block that are on the outer boundary of the clear area.
     * A face is rendered only if the neighbor in that direction is NOT in the clear set.
     */
    private static void renderBoundaryFaces(BufferBuilder buffer, Matrix4f matrix,
                                             BlockPos pos, Set<BlockPos> clearSet,
                                             float x, float y, float z,
                                             int r, int g, int b, int a) {
        float x0 = x, y0 = y, z0 = z;
        float x1 = x + 1, y1 = y + 1, z1 = z + 1;

        if (!clearSet.contains(pos.below())) {
            buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a);
        }
        if (!clearSet.contains(pos.above())) {
            buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a);
        }
        if (!clearSet.contains(pos.north())) {
            buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
        }
        if (!clearSet.contains(pos.south())) {
            buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a);
        }
        if (!clearSet.contains(pos.west())) {
            buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
        }
        if (!clearSet.contains(pos.east())) {
            buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a);
        }
    }

    private static void renderFullBox(BufferBuilder buffer, Matrix4f matrix,
                                       float x0, float y0, float z0,
                                       float x1, float y1, float z1,
                                       int r, int g, int b, int a) {
        // Bottom
        buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a);
        // Top
        buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a);
        // North
        buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
        // South
        buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a);
        // West
        buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
        // East
        buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a);
    }

    private static void renderWireframes(List<Map.Entry<BlockPos, Boolean>> positions, BlockPos blockPos,
                                          PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());

        // Only wireframe blocked blocks so they stand out clearly
        for (Map.Entry<BlockPos, Boolean> entry : positions) {
            if (entry.getValue()) continue; // skip clear blocks

            BlockPos pos = entry.getKey();
            double x = pos.getX() - blockPos.getX();
            double y = pos.getY() - blockPos.getY();
            double z = pos.getZ() - blockPos.getZ();

            AABB box = new AABB(x, y, z, x + 1, y + 1, z + 1);
            LevelRenderer.renderLineBox(poseStack, lineConsumer, box, 1.0f, 0.0f, 0.0f, 1.0f);
        }
    }

}
