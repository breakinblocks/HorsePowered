package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders the working area highlight for horse-powered blocks as a single
 * combined outline rather than individual block boxes.
 * Only outer boundary faces are drawn, so the area reads as one cohesive shape.
 */
public class WorkingAreaRenderer {

    // Low-opacity green for clear area, brighter red for obstructed blocks
    private static final int CLEAR_R = 50, CLEAR_G = 200, CLEAR_B = 50, CLEAR_A = 25;
    private static final int BLOCKED_R = 255, BLOCKED_G = 40, BLOCKED_B = 40, BLOCKED_A = 50;

    public static void render(boolean showHighlight, List<Map.Entry<BlockPos, Boolean>> positions,
                              BlockPos blockPos, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!showHighlight || positions == null || positions.isEmpty()) {
            return;
        }

        // Build a set of all clear positions for neighbor lookup
        Set<BlockPos> clearPositions = new HashSet<>();
        for (Map.Entry<BlockPos, Boolean> entry : positions) {
            if (entry.getValue()) {
                clearPositions.add(entry.getKey());
            }
        }

        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugQuads(),
                (pose, consumer) -> {
                    for (Map.Entry<BlockPos, Boolean> entry : positions) {
                        BlockPos pos = entry.getKey();
                        boolean isClear = entry.getValue();

                        float x = pos.getX() - blockPos.getX();
                        float y = pos.getY() - blockPos.getY();
                        float z = pos.getZ() - blockPos.getZ();

                        if (isClear) {
                            // Only render faces on the boundary of the clear area
                            renderBoundaryFaces(consumer, pose, pos, clearPositions,
                                    x, y, z, CLEAR_R, CLEAR_G, CLEAR_B, CLEAR_A);
                        } else {
                            // Blocked blocks render as individual highlighted boxes
                            renderFullBox(consumer, pose,
                                    x + 0.02f, y + 0.02f, z + 0.02f,
                                    x + 0.98f, y + 0.98f, z + 0.98f,
                                    BLOCKED_R, BLOCKED_G, BLOCKED_B, BLOCKED_A);
                        }
                    }
                }
        );
    }

    /**
     * Renders only the faces of a block that are on the outer boundary of the clear area.
     * A face is rendered only if the neighbor in that direction is NOT in the clear set.
     */
    private static void renderBoundaryFaces(VertexConsumer buffer, PoseStack.Pose pose,
                                             BlockPos pos, Set<BlockPos> clearSet,
                                             float x, float y, float z,
                                             int r, int g, int b, int a) {
        float x0 = x, y0 = y, z0 = z;
        float x1 = x + 1, y1 = y + 1, z1 = z + 1;

        // Bottom face — only if block below is not in the clear area
        if (!clearSet.contains(pos.below())) {
            quad(buffer, pose, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, r,g,b,a);
        }
        // Top face
        if (!clearSet.contains(pos.above())) {
            quad(buffer, pose, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0, r,g,b,a);
        }
        // North face (z-)
        if (!clearSet.contains(pos.north())) {
            quad(buffer, pose, x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0, r,g,b,a);
        }
        // South face (z+)
        if (!clearSet.contains(pos.south())) {
            quad(buffer, pose, x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1, r,g,b,a);
        }
        // West face (x-)
        if (!clearSet.contains(pos.west())) {
            quad(buffer, pose, x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0, r,g,b,a);
        }
        // East face (x+)
        if (!clearSet.contains(pos.east())) {
            quad(buffer, pose, x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1, r,g,b,a);
        }
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                              float x0, float y0, float z0,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float x3, float y3, float z3,
                              int r, int g, int b, int a) {
        buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a);
        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a);
    }

    /**
     * Renders a full box with all 6 faces (for obstructed blocks).
     */
    private static void renderFullBox(VertexConsumer buffer, PoseStack.Pose pose,
                                       float x0, float y0, float z0,
                                       float x1, float y1, float z1,
                                       int r, int g, int b, int a) {
        // Bottom
        quad(buffer, pose, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, r,g,b,a);
        // Top
        quad(buffer, pose, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0, r,g,b,a);
        // North
        quad(buffer, pose, x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0, r,g,b,a);
        // South
        quad(buffer, pose, x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1, r,g,b,a);
        // West
        quad(buffer, pose, x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0, r,g,b,a);
        // East
        quad(buffer, pose, x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1, r,g,b,a);
    }
}
