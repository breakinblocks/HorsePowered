package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class ChopperBlockEntityRenderer implements BlockEntityRenderer<ChopperBlockEntity> {

    private static final ResourceLocation STONE_TEXTURE = ResourceLocation.withDefaultNamespace("block/stone");

    // Blade dimensions (in blocks)
    private static final float BLADE_WIDTH = 0.625F;   // 10/16
    private static final float BLADE_DEPTH = 0.125F;   // 2/16
    private static final float BLADE_HEIGHT = 0.1875F; // 3/16

    // Blade position constants
    private static final float BLADE_MIN_Y = 0.5F;     // Lowest position (at chopping surface - top of oak slab)
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

        // Render the chopping blade
        renderBlade(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(BlockChopper.FACING);
        float rotation = getRotation(facing);

        // Render input item on the chopping surface (oak slab top is at Y=0.5)
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.52D, 0.5D);
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);
            poseStack.popPose();

            if (input.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, input.getCount(), 0.5D, 0.85D, 0.5D);
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

            if (output.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, output.getCount(), 0.5D, 0.55D, 0.5D);
            }
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

    @SuppressWarnings("deprecation")
    private void renderBlade(ChopperBlockEntity blockEntity, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Get the visual windup progress (-0.74 to 0, where 0 is fully raised)
        float windup = blockEntity.getVisualWindup();

        // Convert windup to Y position (windup of -0.74 = min Y, windup of 0 = max Y)
        // Normalize: (-0.74 to 0) -> (0 to 1)
        float progress = (windup + 0.74F) / 0.74F;
        float bladeY = BLADE_MIN_Y + (BLADE_MAX_Y - BLADE_MIN_Y) * progress;

        // Get stone texture from block atlas
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(STONE_TEXTURE);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        poseStack.pushPose();

        // Center the blade - swap width/depth so blade extends along Z axis (rotated 90 degrees)
        float halfWidth = BLADE_WIDTH / 2;
        float halfDepth = BLADE_DEPTH / 2;

        float minX = 0.5F - halfDepth;
        float maxX = 0.5F + halfDepth;
        float minY = bladeY;
        float maxY = bladeY + BLADE_HEIGHT;
        float minZ = 0.5F - halfWidth;
        float maxZ = 0.5F + halfWidth;

        Matrix4f pose = poseStack.last().pose();

        // Get UV coordinates for the full texture
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face (Y+)
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u0, v1, 0, 1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v1, 0, 1, 0, packedLight, packedOverlay);

        // Bottom face (Y-)
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v0, 0, -1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u1, v0, 0, -1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 0, -1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u0, v1, 0, -1, 0, packedLight, packedOverlay);

        // North face (Z-)
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, 0, -1, packedLight, packedOverlay);

        // South face (Z+)
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 0, 0, 1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u0, v1, 0, 0, 1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 0, 0, 1, packedLight, packedOverlay);

        // West face (X-)
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u1, v0, -1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, -1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v1, -1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u1, v1, -1, 0, 0, packedLight, packedOverlay);

        // East face (X+)
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, packedLight, packedOverlay);

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
