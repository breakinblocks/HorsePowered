package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Shared utility for rendering the working area highlight for horse-powered blocks.
 * Displays green boxes for clear areas and red boxes for obstructed areas.
 * TODO: Update for 1.21.11 render system changes
 */
public class WorkingAreaRenderer {

    /**
     * Renders the working area highlight if the block entity should show it.
     * TODO: Reimplement for 1.21.11 render system - RenderSystem methods changed
     */
    public static void renderIfActive(HPBlockEntityHorseBase blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        // Disabled until new render system is understood
        // RenderSystem.enableBlend(), disableCull(), depthMask(), setShader() are no longer available
    }

    /**
     * Renders the working area highlight.
     * TODO: Reimplement for 1.21.11 render system
     */
    public static void render(HPBlockEntityHorseBase blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        // Disabled until new render system is understood
    }
}
