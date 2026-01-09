package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Renders fluid in a tank for the horse-powered press.
 * Uses the 1.21.9+ submitCustomGeometry API for rendering.
 */
public class FluidRenderer {

    // Tank dimensions (in block units, 1/16)
    private static final float TANK_MIN_X = 3f / 16f;
    private static final float TANK_MAX_X = 13f / 16f;
    private static final float TANK_MIN_Y = 1f / 16f;  // Bottom of tank
    private static final float TANK_MAX_Y = 15f / 16f; // Top of tank (max fill)
    private static final float TANK_MIN_Z = 3f / 16f;
    private static final float TANK_MAX_Z = 13f / 16f;

    // Small inset to prevent z-fighting with tank walls
    private static final float INSET = 0.001f;

    /**
     * Renders fluid in the press tank.
     *
     * @param poseStack The pose stack (already translated to block position)
     * @param collector The node collector for submitting geometry
     * @param fluidStack The fluid to render
     * @param tankCapacity The tank's maximum capacity
     * @param packedLight The packed light level
     */
    public static void renderFluid(PoseStack poseStack, SubmitNodeCollector collector,
                                    FluidStack fluidStack, int tankCapacity, int packedLight) {
        if (fluidStack.isEmpty() || tankCapacity <= 0) return;

        // Calculate fill level
        float fillRatio = (float) fluidStack.getAmount() / tankCapacity;
        fillRatio = Math.max(0.01f, Math.min(1.0f, fillRatio)); // Clamp between 0.01 and 1.0

        // Get fluid color
        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        int color = fluidExtensions.getTintColor(fluidStack);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 255; // Default to fully opaque if alpha is 0

        // Calculate fluid surface height
        float fluidMinY = TANK_MIN_Y + INSET;
        float fluidMaxY = TANK_MIN_Y + (TANK_MAX_Y - TANK_MIN_Y) * fillRatio;

        float minX = TANK_MIN_X + INSET;
        float maxX = TANK_MAX_X - INSET;
        float minZ = TANK_MIN_Z + INSET;
        float maxZ = TANK_MAX_Z - INSET;

        final int finalR = r;
        final int finalG = g;
        final int finalB = b;
        final int finalA = a;
        final float finalFluidMaxY = fluidMaxY;

        // Submit custom geometry for the fluid
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugQuads(),
                (pose, consumer) -> {
                    renderFluidBox(consumer, pose,
                            minX, fluidMinY, minZ,
                            maxX, finalFluidMaxY, maxZ,
                            finalR, finalG, finalB, finalA);
                }
        );
    }

    /**
     * Renders a box for the fluid using quads.
     */
    private static void renderFluidBox(VertexConsumer buffer, PoseStack.Pose pose,
                                        float minX, float minY, float minZ,
                                        float maxX, float maxY, float maxZ,
                                        int r, int g, int b, int a) {
        // Slightly darker color for side faces
        int rDark = (int)(r * 0.8f);
        int gDark = (int)(g * 0.8f);
        int bDark = (int)(b * 0.8f);

        // Top face (fluid surface) - brightest
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);

        // Bottom face - darker (usually not visible)
        buffer.addVertex(pose, minX, minY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, minY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, minX, minY, maxZ).setColor(rDark, gDark, bDark, a);

        // North face (z = minZ)
        buffer.addVertex(pose, minX, minY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, minX, maxY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, minY, minZ).setColor(rDark, gDark, bDark, a);

        // South face (z = maxZ)
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, minX, minY, maxZ).setColor(rDark, gDark, bDark, a);

        // West face (x = minX)
        buffer.addVertex(pose, minX, minY, maxZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, minX, maxY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, minX, minY, minZ).setColor(rDark, gDark, bDark, a);

        // East face (x = maxX)
        buffer.addVertex(pose, maxX, minY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(rDark, gDark, bDark, a);
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(rDark, gDark, bDark, a);
    }
}
