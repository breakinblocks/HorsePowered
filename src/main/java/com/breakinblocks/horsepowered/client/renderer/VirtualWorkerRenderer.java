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

public class VirtualWorkerRenderer {

    // entity must be a detached/cached instance — the EntityRenderDispatcher is invoked directly
    // from inside a BE renderer so it never enters the world render pipeline.
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
