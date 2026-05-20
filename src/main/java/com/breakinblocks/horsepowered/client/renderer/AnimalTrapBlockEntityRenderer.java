package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.AnimalTrapBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AnimalTrapBlockEntityRenderer implements BlockEntityRenderer<AnimalTrapBlockEntity> {

    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;

    public AnimalTrapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.getEntityRenderer();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(AnimalTrapBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (be.hasCapturedEntity()) {
            Entity entity = be.getOrBuildDisplayEntity();
            if (entity != null) {
                renderEntity(entity, partialTick, poseStack, bufferSource, packedLight, be);
            }
            return;
        }

        ItemStack bait = be.getInventory().getStackInSlot(AnimalTrapBlockEntity.BAIT_SLOT);
        if (!bait.isEmpty()) {
            renderBait(bait, partialTick, poseStack, bufferSource, packedLight, packedOverlay, be);
        }
    }

    private void renderEntity(Entity entity, float partialTick, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, AnimalTrapBlockEntity be) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);

        float baseScale = 0.53125F;
        float maxDim = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (maxDim > 1.0F) baseScale /= maxDim;

        poseStack.translate(0.0F, 0.4F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Mth.lerp(partialTick, be.getOSpin(), be.getSpin()) * 10.0F));
        poseStack.translate(0.0F, -0.2F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(baseScale, baseScale, baseScale);

        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, partialTick,
                poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    private void renderBait(ItemStack bait, float partialTick, PoseStack poseStack,
                            MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                            AnimalTrapBlockEntity be) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Mth.lerp(partialTick, be.getOSpin(), be.getSpin()) * 10.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        itemRenderer.renderStatic(bait, ItemDisplayContext.GROUND, packedLight, packedOverlay,
                poseStack, bufferSource, be.getLevel(), 0);

        poseStack.popPose();
    }
}
