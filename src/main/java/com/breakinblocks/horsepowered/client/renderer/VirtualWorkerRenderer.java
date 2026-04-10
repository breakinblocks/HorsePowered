package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Renders a virtual (non-world) entity from within a block entity renderer.
 * Uses EntityRenderDispatcher to render the entity at a given offset from the block position.
 */
public class VirtualWorkerRenderer {

    /**
     * Renders a cached entity at the given offset from the block entity's origin.
     *
     * @param entity The cached entity to render (not in the world)
     * @param offsetX X offset from block origin to entity position
     * @param offsetY Y offset from block origin to entity position
     * @param offsetZ Z offset from block origin to entity position
     * @param yRot Entity Y rotation (already interpolated for this frame)
     * @param partialTick Partial tick for interpolation
     * @param poseStack Pose stack (already translated to block position)
     * @param collector Submit node collector
     * @param camera Camera render state
     * @param packedLight Packed light value
     */
    @SuppressWarnings("unchecked")
    public static void renderEntity(Entity entity, double offsetX, double offsetY, double offsetZ,
                                     float yRot, float partialTick,
                                     PoseStack poseStack, SubmitNodeCollector collector,
                                     CameraRenderState camera, int packedLight) {
        if (entity == null) return;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<Entity, EntityRenderState> renderer =
                (EntityRenderer<Entity, EntityRenderState>) dispatcher.getRenderer(entity);
        if (renderer == null) return;

        try {
            // Set rotation on the entity RIGHT BEFORE extractRenderState reads it.
            // We set both O and current to the SAME already-interpolated value so the
            // entity renderer's internal lerp always produces exactly this rotation,
            // regardless of partialTick. This eliminates the double-interpolation
            // artifact that caused single-tick 180° flips.
            entity.yRotO = yRot;
            entity.setYRot(yRot);
            if (entity instanceof LivingEntity living) {
                living.yBodyRotO = yRot;
                living.yBodyRot = yRot;
                living.yHeadRotO = yRot;
                living.yHeadRot = yRot;
            }

            // Create render state and extract from entity
            EntityRenderState renderState = renderer.createRenderState();
            renderer.extractRenderState(entity, renderState, partialTick);

            poseStack.pushPose();
            // Translate to the virtual entity's position relative to block origin
            poseStack.translate(offsetX, offsetY, offsetZ);

            // Render using the entity renderer
            renderer.submit(renderState, poseStack, collector, camera);

            poseStack.popPose();
        } catch (Exception e) {
            // Silently fail - don't crash the game if entity rendering fails
        }
    }
}
