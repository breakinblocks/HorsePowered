package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;

/**
 * Renders a lead (rope) between a horse-powered block and its attached worker mob.
 * Uses the 1.21.9+ submitCustomGeometry API for rendering with quads for proper thickness.
 */
public class LeadRenderer {

    // Leash colors (brownish rope color, similar to vanilla)
    private static final int LEASH_COLOR_R_DARK = 0x55;
    private static final int LEASH_COLOR_G_DARK = 0x33;
    private static final int LEASH_COLOR_B_DARK = 0x11;
    private static final int LEASH_COLOR_R_LIGHT = 0x88;
    private static final int LEASH_COLOR_G_LIGHT = 0x66;
    private static final int LEASH_COLOR_B_LIGHT = 0x33;

    // Rope thickness (half-width)
    private static final float ROPE_HALF_WIDTH = 0.025f;

    /**
     * Renders a lead from a block attachment point to an entity position.
     * Both positions should be relative to the block entity origin (0,0,0).
     *
     * @param blockAttachment The attachment point on the block, relative to block origin
     * @param entityPos The position of the entity's leash attachment point, relative to block origin
     * @param poseStack The pose stack for transformations (already translated to block position)
     * @param collector The node collector for submitting geometry
     */
    public static void renderLead(Vec3 blockAttachment, Vec3 entityPos,
                                   PoseStack poseStack, SubmitNodeCollector collector) {
        if (entityPos == null || blockAttachment == null) return;

        // Calculate the delta from block attachment to entity
        double dx = entityPos.x - blockAttachment.x;
        double dy = entityPos.y - blockAttachment.y;
        double dz = entityPos.z - blockAttachment.z;

        // Translate to block attachment point
        poseStack.pushPose();
        poseStack.translate(blockAttachment.x, blockAttachment.y, blockAttachment.z);

        // Submit custom geometry for the leash using quads for proper thickness
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugQuads(),
                (pose, consumer) -> {
                    renderLeashRibbon(consumer, pose, dx, dy, dz);
                }
        );

        poseStack.popPose();
    }

    /**
     * Renders the leash as a ribbon of quads with proper thickness.
     * Creates two perpendicular ribbons (cross shape) for visibility from any angle.
     */
    private static void renderLeashRibbon(VertexConsumer buffer, PoseStack.Pose pose,
                                           double dx, double dy, double dz) {
        // Number of segments in the leash
        int segments = 24;

        // Calculate distance for sag amount
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float sagAmount = Math.min(distance * 0.15f, 1.5f);

        // Pre-calculate all points along the curve
        float[] xPoints = new float[segments + 1];
        float[] yPoints = new float[segments + 1];
        float[] zPoints = new float[segments + 1];

        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            xPoints[i] = (float) dx * t;
            yPoints[i] = (float) dy * t;
            zPoints[i] = (float) dz * t;

            // Add sag (parabolic curve, maximum at middle)
            float sagT = t * (1 - t) * 4;
            yPoints[i] -= sagAmount * sagT;
        }

        // Render horizontal ribbon (width in X-Z plane)
        for (int i = 0; i < segments; i++) {
            float x1 = xPoints[i];
            float y1 = yPoints[i];
            float z1 = zPoints[i];
            float x2 = xPoints[i + 1];
            float y2 = yPoints[i + 1];
            float z2 = zPoints[i + 1];

            // Calculate perpendicular vector in X-Z plane
            float segDx = x2 - x1;
            float segDz = z2 - z1;
            float segLenXZ = (float) Math.sqrt(segDx * segDx + segDz * segDz);

            float perpX, perpZ;
            if (segLenXZ > 0.0001f) {
                // Perpendicular in X-Z plane
                perpX = -segDz / segLenXZ * ROPE_HALF_WIDTH;
                perpZ = segDx / segLenXZ * ROPE_HALF_WIDTH;
            } else {
                // Fallback for vertical segments
                perpX = ROPE_HALF_WIDTH;
                perpZ = 0;
            }

            // Alternate colors for the rope texture effect
            int r, g, b;
            if (i % 2 == 0) {
                r = LEASH_COLOR_R_DARK;
                g = LEASH_COLOR_G_DARK;
                b = LEASH_COLOR_B_DARK;
            } else {
                r = LEASH_COLOR_R_LIGHT;
                g = LEASH_COLOR_G_LIGHT;
                b = LEASH_COLOR_B_LIGHT;
            }

            // Render quad (4 vertices forming a ribbon segment)
            // Front face
            buffer.addVertex(pose, x1 - perpX, y1, z1 - perpZ).setColor(r, g, b, 255);
            buffer.addVertex(pose, x1 + perpX, y1, z1 + perpZ).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2 + perpX, y2, z2 + perpZ).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2 - perpX, y2, z2 - perpZ).setColor(r, g, b, 255);

            // Back face (reverse winding for double-sided)
            buffer.addVertex(pose, x1 + perpX, y1, z1 + perpZ).setColor(r, g, b, 255);
            buffer.addVertex(pose, x1 - perpX, y1, z1 - perpZ).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2 - perpX, y2, z2 - perpZ).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2 + perpX, y2, z2 + perpZ).setColor(r, g, b, 255);
        }

        // Render vertical ribbon (width in Y direction) for cross-section visibility
        for (int i = 0; i < segments; i++) {
            float x1 = xPoints[i];
            float y1 = yPoints[i];
            float z1 = zPoints[i];
            float x2 = xPoints[i + 1];
            float y2 = yPoints[i + 1];
            float z2 = zPoints[i + 1];

            // Alternate colors
            int r, g, b;
            if (i % 2 == 0) {
                r = LEASH_COLOR_R_LIGHT;
                g = LEASH_COLOR_G_LIGHT;
                b = LEASH_COLOR_B_LIGHT;
            } else {
                r = LEASH_COLOR_R_DARK;
                g = LEASH_COLOR_G_DARK;
                b = LEASH_COLOR_B_DARK;
            }

            // Vertical offset
            float perpY = ROPE_HALF_WIDTH;

            // Front face
            buffer.addVertex(pose, x1, y1 - perpY, z1).setColor(r, g, b, 255);
            buffer.addVertex(pose, x1, y1 + perpY, z1).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2, y2 + perpY, z2).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2, y2 - perpY, z2).setColor(r, g, b, 255);

            // Back face
            buffer.addVertex(pose, x1, y1 + perpY, z1).setColor(r, g, b, 255);
            buffer.addVertex(pose, x1, y1 - perpY, z1).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2, y2 - perpY, z2).setColor(r, g, b, 255);
            buffer.addVertex(pose, x2, y2 + perpY, z2).setColor(r, g, b, 255);
        }
    }
}
