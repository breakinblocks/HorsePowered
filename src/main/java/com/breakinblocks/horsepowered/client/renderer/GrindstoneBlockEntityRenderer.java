package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class GrindstoneBlockEntityRenderer implements BlockEntityRenderer<GrindstoneBlockEntity> {

    private static final double GRINDER_TOP = 0.5D;

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

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);
        ItemStack secondary = blockEntity.getItem(2);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer,
                input, 0.5, 1.1, 0.5, 0.5F, 0, packedLight, packedOverlay,
                blockEntity.getLevel());

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer,
                output, 0.5, 0.3, 0.25, 0.3F, 0, packedLight, packedOverlay,
                blockEntity.getLevel());

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer,
                secondary, 0.5, 0.3, 0.75, 0.3F, 0, packedLight, packedOverlay,
                blockEntity.getLevel());

        if (RenderUtils.shouldShowItemCounts(blockEntity)) {
            double countY = RenderUtils.itemTop(itemRenderer, input, blockEntity.getLevel(), 1.1, 0.5F, false);
            RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, input.getCount(), 0.5, countY, 0.5);
            RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, output.getCount(), 0.5, GRINDER_TOP, 0.25);
            RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, secondary.getCount(), 0.5, GRINDER_TOP, 0.75);
        }
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
