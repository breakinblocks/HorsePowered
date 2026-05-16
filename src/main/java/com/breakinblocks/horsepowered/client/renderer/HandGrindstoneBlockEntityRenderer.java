package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockHandGrindstone;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class HandGrindstoneBlockEntityRenderer implements BlockEntityRenderer<HandGrindstoneBlockEntity> {

    private static final double SIDE_Y = 0.3;
    private static final float SIDE_SCALE = 0.3F;
    private static final double SIDE_COUNT_Y = 0.55;

    private final ItemRenderer itemRenderer;
    private final Font font;

    public HandGrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(HandGrindstoneBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction front = blockEntity.getBlockState().getValue(BlockHandGrindstone.FACING);
        Direction inputSide = front.getClockWise();
        Direction outputSide = front.getCounterClockWise();

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);
        ItemStack secondary = blockEntity.getItem(2);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                input, 0.5, 0.9, 0.5, 0.4F, blockEntity.getVisibleRotation(),
                packedLight, packedOverlay, blockEntity.getLevel(), 1.15);

        renderSide(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, input, inputSide);
        renderSide(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, output, outputSide);
        renderSide(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, secondary, front);
    }

    private void renderSide(HandGrindstoneBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay, ItemStack stack, Direction side) {
        double x = 0.5 + side.getStepX() * 0.25;
        double z = 0.5 + side.getStepZ() * 0.25;
        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                stack, x, SIDE_Y, z, SIDE_SCALE, 0,
                packedLight, packedOverlay, blockEntity.getLevel(), SIDE_COUNT_Y);
    }
}
