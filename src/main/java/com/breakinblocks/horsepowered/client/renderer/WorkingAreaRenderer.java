package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

/**
 * Shared utility for rendering the working area highlight for horse-powered blocks.
 * Displays green boxes for clear areas and red boxes for obstructed areas.
 */
public class WorkingAreaRenderer {

    // Colors for clear (green) and obstructed (red) areas - with transparency
    private static final int CLEAR_R = 0, CLEAR_G = 255, CLEAR_B = 0, CLEAR_A = 80;
    private static final int BLOCKED_R = 255, BLOCKED_G = 0, BLOCKED_B = 0, BLOCKED_A = 80;

    /**
     * Renders the working area highlight using pre-extracted render state data.
     * This version is compatible with the 1.21.9+ render state extraction pattern.
     *
     * @param showHighlight Whether to show the highlight (from render state)
     * @param positions The working area positions with clear/blocked status
     * @param blockPos The block position (for calculating offsets)
     * @param poseStack The pose stack (already translated to block position)
     * @param collector The node collector for submitting geometry
     */
    public static void render(boolean showHighlight, List<Map.Entry<BlockPos, Boolean>> positions,
                              BlockPos blockPos, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!showHighlight || positions == null || positions.isEmpty()) {
            return;
        }

        // Submit custom geometry for all highlight boxes
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugQuads(),
                (pose, consumer) -> {
                    for (Map.Entry<BlockPos, Boolean> entry : positions) {
                        BlockPos pos = entry.getKey();
                        boolean isClear = entry.getValue();

                        // Calculate offset from block entity position
                        float x = pos.getX() - blockPos.getX();
                        float y = pos.getY() - blockPos.getY();
                        float z = pos.getZ() - blockPos.getZ();

                        // Render a slightly smaller box (0.02 inset) to avoid z-fighting
                        renderBox(consumer, pose, x + 0.02f, y + 0.02f, z + 0.02f,
                                x + 0.98f, y + 0.98f, z + 0.98f, isClear);
                    }
                }
        );
    }

    /**
     * Renders a colored box using quads.
     */
    private static void renderBox(VertexConsumer buffer, PoseStack.Pose pose,
                                   float minX, float minY, float minZ,
                                   float maxX, float maxY, float maxZ,
                                   boolean isClear) {
        int r = isClear ? CLEAR_R : BLOCKED_R;
        int g = isClear ? CLEAR_G : BLOCKED_G;
        int b = isClear ? CLEAR_B : BLOCKED_B;
        int a = isClear ? CLEAR_A : BLOCKED_A;

        // Bottom face (y = minY)
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);

        // Top face (y = maxY)
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);

        // North face (z = minZ)
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);

        // South face (z = maxZ)
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);

        // West face (x = minX)
        buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);

        // East face (x = maxX)
        buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
    }
}
