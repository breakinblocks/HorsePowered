package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ChopperBlockEntityRenderer implements BlockEntityRenderer<ChopperBlockEntity> {

    private static final float BLADE_MIN_Y = 0.5F;
    private static final float BLADE_MAX_Y = 1.25F;

    private final ItemRenderer itemRenderer;
    private final Font font;

    public ChopperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(ChopperBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        // On the frame after removal the state can already be AIR (no FACING property); skipping avoids
        // a missing-texture particle burst from getValue throwing.
        if (blockEntity.isRemoved() || !state.hasProperty(BlockChopper.FACING)) {
            return;
        }

        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);

        Direction facing = state.getValue(BlockChopper.FACING);
        float rotation = getRotation(facing);

        renderBlade(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, rotation);

        // Log stands upright on the chopping surface so the blade cleaves it like a real axe chop.
        RenderUtils.renderStandingItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(0), 0.5, 0.71, 0.5, 0.6F, rotation, packedLight, packedOverlay,
                blockEntity.getLevel(), 1.05);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(1), 0.5, 0.3, 0.5, 0.4F, rotation + 45, packedLight, packedOverlay,
                blockEntity.getLevel(), 0.55);
    }

    private void renderBlade(ChopperBlockEntity blockEntity, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay, float rotation) {
        // visualWindup is -0.74 (fully wound down) .. 0 (raised); map to [BLADE_MIN_Y..BLADE_MAX_Y].
        float visualWindup = blockEntity.getVisualWindup();
        float bladeTravel = BLADE_MAX_Y - BLADE_MIN_Y;
        float normalizedProgress = (visualWindup + 0.74F) / 0.74F;
        float bladeY = BLADE_MIN_Y + (normalizedProgress * bladeTravel);

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(Blocks.IRON_BLOCK.defaultBlockState())
                .getParticleIcon();

        poseStack.pushPose();

        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5, 0, -0.5);

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        float bladeMinX = 0.1F;
        float bladeMaxX = 0.9F;
        float bladeThickness = 0.05F;
        float bladeHeight = 0.5F;

        float bladeMinZ = 0.5F - (bladeThickness / 2);
        float bladeMaxZ = 0.5F + (bladeThickness / 2);

        RenderUtils.renderTexturedBox(poseStack, builder, sprite, packedLight,
                bladeMinX, bladeY, bladeMinZ,
                bladeMaxX, bladeY + bladeHeight, bladeMaxZ);

        poseStack.popPose();
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

    @Override
    public boolean shouldRenderOffScreen(ChopperBlockEntity blockEntity) {
        return blockEntity.shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
