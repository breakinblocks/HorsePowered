package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Renders a virtual (non-world) entity from within a block entity renderer.
 * Uses EntityRenderDispatcher to render the entity at a given offset from the block position.
 */
public class VirtualWorkerRenderer {

    /**
     * Renders the virtual worker for a horse-powered block entity.
     * Does nothing if no worker is attached.
     * Call this from the block entity renderer's render() method.
     */
    public static void renderWorker(HPBlockEntityHorseBase blockEntity, float partialTick,
                                     PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!blockEntity.hasWorker()) return;

        Entity entity = blockEntity.getCachedRenderEntity();
        if (entity == null) return;

        // Interpolated virtual position relative to block origin
        BlockPos blockPos = blockEntity.getBlockPos();
        double workerX = Mth.lerp(partialTick, blockEntity.getPrevVirtualX(), blockEntity.getVirtualX());
        double workerZ = Mth.lerp(partialTick, blockEntity.getPrevVirtualZ(), blockEntity.getVirtualZ());

        double offsetX = workerX - blockPos.getX();
        double offsetY = blockEntity.getVirtualY() - blockPos.getY();
        double offsetZ = workerZ - blockPos.getZ();

        // Interpolated rotation with angle wrapping
        float prevYRot = blockEntity.getPrevVirtualYRot();
        float yRot = blockEntity.getVirtualYRot();
        float diff = yRot - prevYRot;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        float interpYRot = prevYRot + diff * partialTick;

        renderEntity(entity, offsetX, offsetY, offsetZ, interpYRot, partialTick, poseStack, bufferSource, packedLight);
    }

    /**
     * Renders a cached entity at the given offset from the block entity's origin.
     *
     * @param entity     The cached entity to render (not in the world)
     * @param offsetX    X offset from block origin to entity position
     * @param offsetY    Y offset from block origin to entity position
     * @param offsetZ    Z offset from block origin to entity position
     * @param yRot       Entity Y rotation (already interpolated for this frame)
     * @param partialTick Partial tick for interpolation
     * @param poseStack  Pose stack (already translated to block position)
     * @param bufferSource Multi-buffer source for rendering
     * @param packedLight Packed light value
     */
    public static void renderEntity(Entity entity, double offsetX, double offsetY, double offsetZ,
                                     float yRot, float partialTick,
                                     PoseStack poseStack, MultiBufferSource bufferSource,
                                     int packedLight) {
        if (entity == null) return;

        try {
            // Set rotation on the entity RIGHT BEFORE rendering.
            // Set both O and current to the SAME already-interpolated value so the
            // entity renderer's internal lerp always produces exactly this rotation,
            // regardless of partialTick. This eliminates double-interpolation artifacts.
            entity.setYRot(yRot);
            entity.setXRot(0);
            // setOldPosAndRot syncs base Entity fields (xo/yo/zo, xRotO, yRotO).
            entity.setOldPosAndRot();
            // Must also sync LivingEntity-specific rotation old values explicitly —
            // setOldPosAndRot() doesn't touch yBodyRotO / yHeadRotO, so without this
            // the body would lerp from 0 to yRot every frame (visible 180° flip).
            if (entity instanceof LivingEntity living) {
                living.yBodyRot = yRot;
                living.yBodyRotO = yRot;
                living.yHeadRot = yRot;
                living.yHeadRotO = yRot;
            }

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

            poseStack.pushPose();
            poseStack.translate(offsetX, offsetY, offsetZ);

            // 1.21.1's EntityRenderDispatcher.render() signature:
            // render(Entity, double x, double y, double z, float yRot, float partialTick, PoseStack, MultiBufferSource, int packedLight)
            dispatcher.render(entity, 0, 0, 0, yRot, partialTick, poseStack, bufferSource, packedLight);

            poseStack.popPose();
        } catch (Exception e) {
            // Silently fail - don't crash the game if entity rendering fails
        }
    }
}
