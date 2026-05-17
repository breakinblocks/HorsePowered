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

public class GhostWorkerRenderer {

    public static void render(HPBlockEntityHorseBase blockEntity, float partialTick,
                              PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!blockEntity.hasWorker()) return;
        Entity entity = blockEntity.getCachedRenderEntity();
        if (entity == null) return;

        BlockPos blockPos = blockEntity.getBlockPos();
        double workerX = Mth.lerp(partialTick, blockEntity.getPrevVirtualX(), blockEntity.getVirtualX());
        double workerZ = Mth.lerp(partialTick, blockEntity.getPrevVirtualZ(), blockEntity.getVirtualZ());

        double offsetX = workerX - blockPos.getX();
        double offsetY = blockEntity.getVirtualY() - blockPos.getY();
        double offsetZ = workerZ - blockPos.getZ();

        float prevYRot = blockEntity.getPrevVirtualYRot();
        float yRot = blockEntity.getVirtualYRot();
        float diff = yRot - prevYRot;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        float interpYRot = prevYRot + diff * partialTick;

        entity.setYRot(interpYRot);
        entity.setXRot(0F);
        entity.setOldPosAndRot();
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = interpYRot;
            living.yBodyRotO = interpYRot;
            living.yHeadRot = interpYRot;
            living.yHeadRotO = interpYRot;
        }

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, offsetZ);
        dispatcher.render(entity, 0, 0, 0, interpYRot, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
