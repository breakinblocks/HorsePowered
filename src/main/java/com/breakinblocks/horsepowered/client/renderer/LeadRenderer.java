package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Renders a lead (rope) between a horse-powered block and its attached worker mob.
 * Uses similar technique to vanilla mob leash rendering.
 * TODO: Update for 1.21.11 render system changes
 */
public class LeadRenderer {

    /**
     * Renders a lead from the block entity to its worker if present.
     * TODO: Reimplement for 1.21.11 render system - RenderTypes.leash() may have changed
     */
    public static void renderLead(HPBlockEntityHorseBase blockEntity, float partialTick,
                                   PoseStack poseStack, MultiBufferSource bufferSource) {
        // Disabled until new render system is understood
        // RenderTypes.leash() API may have changed
    }
}
