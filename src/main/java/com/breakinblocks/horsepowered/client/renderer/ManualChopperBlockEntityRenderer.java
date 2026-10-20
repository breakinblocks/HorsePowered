package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class ManualChopperBlockEntityRenderer implements BlockEntityRenderer<ManualChopperBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public ManualChopperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(ManualChopperBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Chopping surface is at y=9/16=0.5625. Stand the log upright with its base on the surface.
        // FIXED scale of 0.5 means a scale of 0.6 yields ~0.3 block height — center at ~0.71.
        ItemStack input = blockEntity.getItem(0);
        RenderUtils.renderStandingItem(poseStack, bufferSource, itemRenderer,
                input, 0.5, 0.71, 0.5, 0.6F, 0, packedLight, packedOverlay,
                blockEntity.getLevel());

        if (RenderUtils.shouldShowItemCounts(blockEntity)) {
            double countY = RenderUtils.itemTop(itemRenderer, input, blockEntity.getLevel(), 0.71, 0.6F, true);
            RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, input.getCount(), 0.5, countY, 0.5);
        }
    }
}
