package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class GrindstoneBlockEntityRenderer implements BlockEntityRenderer<GrindstoneBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public GrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(GrindstoneBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);
        GhostWorkerRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight);
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(0), 0.5, 1.1, 0.5, 0.5F, 0, packedLight, packedOverlay,
                blockEntity.getLevel(), 1.4);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(1), 0.5, 0.3, 0.25, 0.3F, 0, packedLight, packedOverlay,
                blockEntity.getLevel(), 0.55);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(2), 0.5, 0.3, 0.75, 0.3F, 0, packedLight, packedOverlay,
                blockEntity.getLevel(), 0.55);
    }

    @Override
    public boolean shouldRenderOffScreen(GrindstoneBlockEntity blockEntity) {
        // Keep rendering when off-screen so the working-area highlight stays visible while placing.
        return blockEntity.shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
