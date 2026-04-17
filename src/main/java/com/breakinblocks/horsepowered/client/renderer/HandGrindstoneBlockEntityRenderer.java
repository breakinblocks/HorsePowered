package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

/**
 * Renderer for the hand grindstone — displays the item currently being ground
 * on top of the grinding surface.
 */
public class HandGrindstoneBlockEntityRenderer implements BlockEntityRenderer<HandGrindstoneBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public HandGrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(HandGrindstoneBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Block is 14/16 tall — sit the item just above that surface.
        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(0), 0.5, 0.9, 0.5, 0.4F, 0, packedLight, packedOverlay,
                blockEntity.getLevel(), 1.15);
    }
}
