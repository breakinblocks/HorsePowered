package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@EventBusSubscriber(modid = HorsePowerMod.MOD_ID, value = Dist.CLIENT)
public class VirtualWorkerRenderer {

    private static Entity forcedNameTagEntity;

    public static boolean isLookedAt(Entity entity) {
        if (entity == null || !entity.hasCustomName() || !Minecraft.renderNames()) return false;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || entity.isInvisibleTo(player)) return false;

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.entityInteractionRange()));
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
    public static void onCanRenderNameTag(RenderNameTagEvent.CanRender event) {
        if (forcedNameTagEntity != null && event.getEntity() == forcedNameTagEntity) {
            event.setCanRender(TriState.TRUE);
        }
    }

    // entity must be a detached/cached instance — the EntityRenderDispatcher is invoked directly
    // from inside a BE renderer so it never enters the world render pipeline.
    @SuppressWarnings("unchecked")
    public static void renderEntity(
            Entity entity,
            double offsetX,
            double offsetY,
            double offsetZ,
            float yRot,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera,
            int packedLight,
            boolean forceNameTag) {
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
            forcedNameTagEntity = forceNameTag ? entity : null;
            try {
                renderer.extractRenderState(entity, renderState, partialTick);
            } finally {
                forcedNameTagEntity = null;
            }

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
