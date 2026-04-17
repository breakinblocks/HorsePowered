package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

/**
 * Renderer for the hand chopping block — displays the log currently being chopped
 * on top of the station surface.
 */
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
        // Block is 9/16 tall — sit the item just above that surface.
        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(0), 0.5, 0.58, 0.5, 0.5F, 0, packedLight, packedOverlay,
                blockEntity.getLevel(), 0.85);
    }
}
