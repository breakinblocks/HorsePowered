package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class ChopperBlockEntityRenderer implements BlockEntityRenderer<ChopperBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public ChopperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(ChopperBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // Render working area highlight if active
        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);

        // Render lead to attached worker
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(BlockChopper.FACING);
        float rotation = getRotation(facing);

        // Render input item on the chopping surface
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.02D, 0.5D);
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            if (input.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0.0D, 0.0D, -0.3D);
                renderItemCount(poseStack, bufferSource, packedLight, input.getCount());
            }

            poseStack.popPose();
        }

        // Render output item
        if (!output.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.3D, 0.5D);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation + 45));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(output, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            if (output.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0.0D, 0.0D, -0.3D);
                renderItemCount(poseStack, bufferSource, packedLight, output.getCount());
            }

            poseStack.popPose();
        }
    }

    private float getRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> 270;
            default -> 0;
        };
    }

    private void renderItemCount(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int count) {
        poseStack.pushPose();
        poseStack.scale(0.025F, -0.025F, 0.025F);

        String text = String.valueOf(count);
        float x = -font.width(text) / 2.0F;

        Matrix4f matrix = poseStack.last().pose();
        font.drawInBatch(text, x, 0, 0xFFFFFF, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ChopperBlockEntity blockEntity) {
        return blockEntity.shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
