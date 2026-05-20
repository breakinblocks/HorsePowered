package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.AnimalTrapBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AnimalTrapBlockEntityRenderer implements BlockEntityRenderer<AnimalTrapBlockEntity, AnimalTrapBlockEntityRenderer.AnimalTrapRenderState> {

    public AnimalTrapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public AnimalTrapRenderState createRenderState() {
        return new AnimalTrapRenderState();
    }

    @Override
    public void extractRenderState(AnimalTrapBlockEntity be, AnimalTrapRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.spin = Mth.lerp(partialTick, (float) be.getOSpin(), (float) be.getSpin());

        if (be.hasCapturedEntity()) {
            Entity entity = be.getOrBuildDisplayEntity();
            if (entity != null) {
                EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                @SuppressWarnings("unchecked")
                EntityRenderer<Entity, EntityRenderState> renderer =
                        (EntityRenderer<Entity, EntityRenderState>) dispatcher.getRenderer(entity);
                if (renderer != null) {
                    try {
                        net.minecraft.core.BlockPos pos = be.getBlockPos();
                        entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        entity.xOld = entity.getX();
                        entity.yOld = entity.getY();
                        entity.zOld = entity.getZ();
                        EntityRenderState entityState = renderer.createRenderState();
                        renderer.extractRenderState(entity, entityState, partialTick);
                        entityState.lightCoords = state.lightCoords;
                        state.entityRenderer = renderer;
                        state.entityState = entityState;
                        state.entityWidth = entity.getBbWidth();
                        state.entityHeight = entity.getBbHeight();
                        state.baitItem.clear();
                        return;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        state.entityRenderer = null;
        state.entityState = null;
        state.entityWidth = 0;
        state.entityHeight = 0;
        ItemStack bait = be.getItem(AnimalTrapBlockEntity.BAIT_SLOT);
        RenderUtils.extractItemState(state.baitItem, bait, be.getLevel());
    }

    @Override
    public void submit(AnimalTrapRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.entityRenderer != null && state.entityState != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            float baseScale = 0.53125F;
            float maxDim = Math.max(state.entityWidth, state.entityHeight);
            if (maxDim > 1.0F) baseScale /= maxDim;
            poseStack.translate(0.0F, 0.4F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.spin * 10.0F));
            poseStack.translate(0.0F, -0.2F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
            poseStack.scale(baseScale, baseScale, baseScale);
            try {
                @SuppressWarnings({"unchecked", "rawtypes"})
                EntityRenderer raw = state.entityRenderer;
                raw.submit(state.entityState, poseStack, collector, camera);
            } catch (Exception ignored) {
            }
            poseStack.popPose();
            return;
        }

        if (!state.baitItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.45F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.spin * 10.0F));
            poseStack.scale(0.6F, 0.6F, 0.6F);
            state.baitItem.submit(poseStack, collector, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class AnimalTrapRenderState extends BlockEntityRenderState {
        public float spin;
        @Nullable public EntityRenderer<Entity, EntityRenderState> entityRenderer;
        @Nullable public EntityRenderState entityState;
        public float entityWidth;
        public float entityHeight;
        public final ItemStackRenderState baitItem = new ItemStackRenderState();
    }
}
