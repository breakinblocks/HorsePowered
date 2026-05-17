package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class GeneratorBlockEntityRenderer implements BlockEntityRenderer<GeneratorBlockEntity> {

    public GeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);
        GhostWorkerRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight);
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);
    }

    @Override
    public boolean shouldRenderOffScreen(GeneratorBlockEntity blockEntity) {
        return blockEntity.shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
