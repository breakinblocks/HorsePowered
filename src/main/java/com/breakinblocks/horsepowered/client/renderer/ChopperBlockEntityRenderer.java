package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.Configs;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ChopperBlockEntityRenderer implements BlockEntityRenderer<ChopperBlockEntity> {

    // Blade position constants - the blade sits on an oak slab base
    private static final float BLADE_MIN_Y = 0.5F;     // Lowest position (at chopping surface)
    private static final float BLADE_MAX_Y = 1.25F;    // Highest position (raised)

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

        // Render animated blade
        renderBlade(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, rotation);

        // Render input item on the chopping surface (on top of the oak slab base)
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.52D, 0.5D);  // Lower position - on slab surface
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            poseStack.popPose();

            // Render count as billboard if more than 1
            if (input.getCount() > 1 && Configs.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight,
                        input.getCount(), 0.5D, 0.85D, 0.5D);
            }
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

            poseStack.popPose();

            // Render count as billboard if more than 1
            if (output.getCount() > 1 && Configs.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight,
                        output.getCount(), 0.5D, 0.55D, 0.5D);
            }
        }
    }

    private void renderBlade(ChopperBlockEntity blockEntity, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay, float rotation) {
        // Get visual windup from block entity (-0.74 to 0)
        float visualWindup = blockEntity.getVisualWindup();

        // Convert to blade Y position (0.74 range mapped to blade travel distance)
        float bladeTravel = BLADE_MAX_Y - BLADE_MIN_Y;
        // visualWindup: -0.74 = down (min), 0 = up (max)
        float normalizedProgress = (visualWindup + 0.74F) / 0.74F; // 0 to 1
        float bladeY = BLADE_MIN_Y + (normalizedProgress * bladeTravel);

        // Get iron block texture for blade
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(Blocks.IRON_BLOCK.defaultBlockState())
                .getParticleIcon();

        poseStack.pushPose();

        // Rotate blade to match block facing
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5, 0, -0.5);

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        // Blade dimensions (vertical blade - thin in Z, tall in Y)
        float bladeMinX = 0.1F;
        float bladeMaxX = 0.9F;
        float bladeThickness = 0.05F;  // Thin blade
        float bladeHeight = 0.5F;      // Tall blade

        // Center the blade in Z
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
