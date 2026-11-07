package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.blockentity.VirtualWorker;
import com.breakinblocks.horsepowered.blockentity.WorkerHost;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class GhostWorkerRenderer {

    private static Entity forcedNameTagEntity;

    public static void render(WorkerHost blockEntity, float partialTick,
                              PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        VirtualWorker worker = blockEntity.getVirtualWorker();
        if (!worker.hasWorker()) return;
        Entity entity = worker.getCachedRenderEntity();
        if (entity == null) return;

        BlockPos blockPos = blockEntity.getBlockPos();
        double workerX = Mth.lerp(partialTick, worker.getPrevVirtualX(), worker.getVirtualX());
        double workerZ = Mth.lerp(partialTick, worker.getPrevVirtualZ(), worker.getVirtualZ());

        double offsetX = workerX - blockPos.getX();
        double offsetY = worker.getVirtualY() - blockPos.getY();
        double offsetZ = workerZ - blockPos.getZ();

        float prevYRot = worker.getPrevVirtualYRot();
        float yRot = worker.getVirtualYRot();
        float diff = yRot - prevYRot;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        float interpYRot = prevYRot + diff * partialTick;

        entity.setPos(workerX, worker.getVirtualY(), workerZ);
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
        forcedNameTagEntity = isLookedAt(entity) ? entity : null;
        try {
            dispatcher.render(entity, 0, 0, 0, interpYRot, partialTick, poseStack, bufferSource, packedLight);
        } finally {
            forcedNameTagEntity = null;
        }
        poseStack.popPose();
    }

    private static boolean isLookedAt(Entity entity) {
        if (!entity.hasCustomName() || !Minecraft.renderNames()) return false;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || entity.isInvisibleTo(player)) return false;

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.getEntityReach()));
        AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
        Optional<Vec3> hit = box.contains(eye) ? Optional.of(eye) : box.clip(eye, end);
        if (hit.isEmpty()) return false;

        HitResult blockHit = mc.hitResult;
        if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getLocation().distanceToSqr(eye) >= hit.get().distanceToSqr(eye);
        }
        return true;
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (forcedNameTagEntity != null && event.getEntity() == forcedNameTagEntity) {
            event.setResult(Event.Result.ALLOW);
        }
    }
}
