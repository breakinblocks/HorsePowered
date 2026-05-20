package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {

    private static final float FADE_START = 0.7F;
    private static final float FADE_SPAN = 1.0F - FADE_START;

    private final ItemRenderer itemRenderer;
    private final Font font;

    public DryingRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(DryingRackBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!state.hasProperty(BlockDryingRack.FACING)) return;
        Direction facing = state.getValue(BlockDryingRack.FACING);
        Direction rightDir = facing.getClockWise();
        double y = 14.5 / 16.0;

        for (int slot = 0; slot < DryingRackBlockEntity.SLOT_COUNT; slot++) {
            ItemStack stack = be.getItem(slot);
            if (stack.isEmpty()) continue;

            int row = slot / 2;
            int col = slot % 2;
            double[] rowCenters = { 4.0, 12.0, 20.0, 28.0 };
            double[] colCenters = { 10.0, 22.0 };
            double rightPx = colCenters[col];
            double forwardPx = rowCenters[row];

            double rightOffset = (rightPx / 16.0) - 0.5;
            double forwardOffset = (forwardPx / 16.0) - 0.5;

            double x = 0.5 + rightOffset * rightDir.getStepX() + forwardOffset * facing.getStepX();
            double z = 0.5 + rightOffset * rightDir.getStepZ() + forwardOffset * facing.getStepZ();

            int progress = be.getProgress(slot);
            int time = be.getRecipeTime(slot);
            boolean finished = be.isFinished(slot);

            if (finished || progress <= 0 || time <= 0) {
                RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font, stack,
                        x, y, z, 0.45F, 0F, packedLight, packedOverlay, be.getLevel(), y + 0.05);
                continue;
            }

            float effectiveProgress = Math.min((float) time, (float) progress + partialTick);
            float ratio = effectiveProgress / (float) time;
            if (ratio < FADE_START) {
                RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font, stack,
                        x, y, z, 0.45F, 0F, packedLight, packedOverlay, be.getLevel(), y + 0.05);
                continue;
            }

            float fade = (ratio - FADE_START) / FADE_SPAN;
            float inputAlpha = 1F - fade;
            float outputAlpha = fade;
            ItemStack output = be.getOutputPreview(slot);

            RenderUtils.renderFlatItemFading(poseStack, bufferSource, itemRenderer, stack,
                    x, y, z, 0.45F, 0F, packedLight, packedOverlay, be.getLevel(), inputAlpha);
            if (!output.isEmpty()) {
                RenderUtils.renderFlatItemFading(poseStack, bufferSource, itemRenderer, output,
                        x, y, z, 0.45F, 0F, packedLight, packedOverlay, be.getLevel(), outputAlpha);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(DryingRackBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();
        if (!state.hasProperty(BlockDryingRack.FACING)) {
            return new AABB(pos);
        }
        Direction facing = state.getValue(BlockDryingRack.FACING);
        Direction rightDir = facing.getClockWise();
        int minX = Math.min(0, Math.min(facing.getStepX(), rightDir.getStepX() + facing.getStepX()));
        int maxX = Math.max(0, Math.max(facing.getStepX(), rightDir.getStepX() + facing.getStepX()));
        int minZ = Math.min(0, Math.min(facing.getStepZ(), rightDir.getStepZ() + facing.getStepZ()));
        int maxZ = Math.max(0, Math.max(facing.getStepZ(), rightDir.getStepZ() + facing.getStepZ()));
        minX = Math.min(minX, rightDir.getStepX());
        maxX = Math.max(maxX, rightDir.getStepX());
        minZ = Math.min(minZ, rightDir.getStepZ());
        maxZ = Math.max(maxZ, rightDir.getStepZ());
        return new AABB(pos.getX() + minX, pos.getY(), pos.getZ() + minZ,
                        pos.getX() + maxX + 1, pos.getY() + 1, pos.getZ() + maxZ + 1);
    }
}
