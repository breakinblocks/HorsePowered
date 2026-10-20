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
import net.minecraft.world.level.block.state.BlockState;

public class HandGrindstoneBlockEntityRenderer implements BlockEntityRenderer<HandGrindstoneBlockEntity> {

    private static final double SIDE_Y = 0.3;
    private static final float SIDE_SCALE = 0.3F;
    private static final double SIDE_COUNT_Y = 8.25 / 16.0;

    private final ItemRenderer itemRenderer;
    private final Font font;

    public HandGrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(HandGrindstoneBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction front = state.hasProperty(BlockHandGrindstone.FACING)
                ? state.getValue(BlockHandGrindstone.FACING)
                : Direction.NORTH;
        Direction inputSide = front.getClockWise();
        Direction outputSide = front.getCounterClockWise();

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);
        ItemStack secondary = blockEntity.getItem(2);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer,
                input, 0.5, 0.9, 0.5, 0.4F, blockEntity.getVisibleRotation(),
                packedLight, packedOverlay, blockEntity.getLevel());

        renderSide(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, input, inputSide);
        renderSide(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, output, outputSide);
        renderSide(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, secondary, front);

        if (RenderUtils.shouldShowItemCounts(blockEntity)) {
            double countY = RenderUtils.itemTop(itemRenderer, input, blockEntity.getLevel(), 0.9, 0.4F, false);
            RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, input.getCount(), 0.5, countY, 0.5);
            renderSideCount(poseStack, bufferSource, packedLight, output.getCount(), outputSide);
            renderSideCount(poseStack, bufferSource, packedLight, secondary.getCount(), front);
        }
    }

    private void renderSide(HandGrindstoneBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay, ItemStack stack, Direction side) {
        double x = 0.5 + side.getStepX() * 0.25;
        double z = 0.5 + side.getStepZ() * 0.25;
        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer,
                stack, x, SIDE_Y, z, SIDE_SCALE, 0,
                packedLight, packedOverlay, blockEntity.getLevel());
    }

    private void renderSideCount(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                 int count, Direction side) {
        double x = 0.5 + side.getStepX() * 0.25;
        double z = 0.5 + side.getStepZ() * 0.25;
        RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, count, x, SIDE_COUNT_Y, z);
    }
}
