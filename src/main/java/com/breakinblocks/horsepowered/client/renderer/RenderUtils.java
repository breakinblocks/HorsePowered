package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;

public final class RenderUtils {

    private static final float COUNT_SCALE = 0.025F;
    private static final double COUNT_GAP = 0.03D;

    private RenderUtils() {
    }

    public static void renderFlatItem(PoseStack poseStack, MultiBufferSource bufferSource,
                                       ItemRenderer itemRenderer, ItemStack stack,
                                       double x, double y, double z, float scale, float yRotation,
                                       int packedLight, int packedOverlay, Level level) {
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        if (yRotation != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, level, 0);

        poseStack.popPose();
    }

    public static void renderFlatItemFading(PoseStack poseStack, MultiBufferSource bufferSource,
                                            ItemRenderer itemRenderer, ItemStack stack,
                                            double x, double y, double z, float scale, float yRotation,
                                            int packedLight, int packedOverlay, Level level, float alpha) {
        if (stack.isEmpty() || alpha <= 0F) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        if (yRotation != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        MultiBufferSource alphaSource = new TransparentMultiBufferSource(bufferSource, alpha);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, alphaSource, level, 0);

        poseStack.popPose();
    }

    public static void renderStandingItem(PoseStack poseStack, MultiBufferSource bufferSource,
                                           ItemRenderer itemRenderer, ItemStack stack,
                                           double x, double y, double z, float scale, float yRotation,
                                           int packedLight, int packedOverlay, Level level) {
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        if (yRotation != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, level, 0);

        poseStack.popPose();
    }

    public static boolean shouldShowItemCounts(BlockEntity blockEntity) {
        if (!HorsePowerConfig.renderItemAmount.get()) return false;
        if (!HorsePowerConfig.mustLookAtBlock.get()) return true;
        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        if (pos.equals(blockEntity.getBlockPos())) return true;
        Level level = blockEntity.getLevel();
        return level != null && level.getBlockEntity(pos) instanceof FillerBlockEntity filler
                && filler.getFilledTileEntity() == blockEntity;
    }

    public static double itemTop(ItemRenderer itemRenderer, ItemStack stack, Level level,
                                 double y, float scale, boolean standing) {
        if (stack.isEmpty()) return y;
        double halfHeight;
        if (itemRenderer.getModel(stack, level, null, 0).isGui3d()) {
            halfHeight = 0.25D;
        } else {
            halfHeight = standing ? 0.5D : 0.03125D;
        }
        return y + halfHeight * scale;
    }

    /**
     * Renders item count as a billboard that always faces the camera, like name tags.
     *
     * @param poseStack    The pose stack for transformations
     * @param bufferSource The buffer source for rendering
     * @param font         The font to use for rendering text
     * @param packedLight  The packed light value
     * @param count        The item count to display
     * @param x            X offset from block origin
     * @param y            Y offset from block origin
     * @param z            Z offset from block origin
     */
    public static double renderItemCountBillboard(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                                  int packedLight, int count, double x, double y, double z) {
        if (count <= 1) return y;

        poseStack.pushPose();
        poseStack.translate(x, y + COUNT_GAP, z);

        // Billboard rotation - face the camera
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        // Scale down for text
        poseStack.scale(COUNT_SCALE, -COUNT_SCALE, COUNT_SCALE);

        Component text = Component.literal(Integer.toString(count));
        float textX = -font.width(text) / 2.0F;

        Matrix4f matrix = poseStack.last().pose();

        int background = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        font.drawInBatch(text, textX, -font.lineHeight, -1, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, background, packedLight);

        poseStack.popPose();
        return y + COUNT_GAP + (font.lineHeight + 1) * COUNT_SCALE;
    }

    /**
     * Adds a vertex to the buffer with all required attributes.
     *
     * @param builder       The vertex consumer
     * @param pose          The pose matrix
     * @param x             X position
     * @param y             Y position
     * @param z             Z position
     * @param u             U texture coordinate
     * @param v             V texture coordinate
     * @param nx            Normal X component
     * @param ny            Normal Y component
     * @param nz            Normal Z component
     * @param packedLight   Packed light value
     * @param packedOverlay Packed overlay value
     */
    public static void addVertex(VertexConsumer builder, Matrix4f pose,
                                 float x, float y, float z, float u, float v,
                                 float nx, float ny, float nz, int packedLight, int packedOverlay) {
        builder.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(nx, ny, nz);
    }

    /**
     * Renders a textured box (cuboid) with all six faces.
     *
     * @param poseStack     The pose stack
     * @param bufferSource  The buffer source
     * @param textureLocation The texture resource location (from block atlas)
     * @param minX          Minimum X coordinate
     * @param minY          Minimum Y coordinate
     * @param minZ          Minimum Z coordinate
     * @param maxX          Maximum X coordinate
     * @param maxY          Maximum Y coordinate
     * @param maxZ          Maximum Z coordinate
     * @param packedLight   Packed light value
     * @param packedOverlay Packed overlay value
     */
    @SuppressWarnings("deprecation")
    public static void renderTexturedBox(PoseStack poseStack, MultiBufferSource bufferSource,
                                         ResourceLocation textureLocation,
                                         float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ,
                                         int packedLight, int packedOverlay) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(textureLocation);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        Matrix4f pose = poseStack.last().pose();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face (Y+)
        addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 1, 0, packedLight, packedOverlay);
        addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight, packedOverlay);
        addVertex(builder, pose, minX, maxY, maxZ, u0, v1, 0, 1, 0, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, maxY, maxZ, u1, v1, 0, 1, 0, packedLight, packedOverlay);

        // Bottom face (Y-)
        addVertex(builder, pose, minX, minY, minZ, u0, v0, 0, -1, 0, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, minY, minZ, u1, v0, 0, -1, 0, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 0, -1, 0, packedLight, packedOverlay);
        addVertex(builder, pose, minX, minY, maxZ, u0, v1, 0, -1, 0, packedLight, packedOverlay);

        // North face (Z-)
        addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, packedLight, packedOverlay);
        addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, 0, -1, packedLight, packedOverlay);

        // South face (Z+)
        addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 0, 0, 1, packedLight, packedOverlay);
        addVertex(builder, pose, minX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight, packedOverlay);
        addVertex(builder, pose, minX, minY, maxZ, u0, v1, 0, 0, 1, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 0, 0, 1, packedLight, packedOverlay);

        // West face (X-)
        addVertex(builder, pose, minX, maxY, maxZ, u1, v0, -1, 0, 0, packedLight, packedOverlay);
        addVertex(builder, pose, minX, maxY, minZ, u0, v0, -1, 0, 0, packedLight, packedOverlay);
        addVertex(builder, pose, minX, minY, minZ, u0, v1, -1, 0, 0, packedLight, packedOverlay);
        addVertex(builder, pose, minX, minY, maxZ, u1, v1, -1, 0, 0, packedLight, packedOverlay);

        // East face (X+)
        addVertex(builder, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, packedLight, packedOverlay);
        addVertex(builder, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, packedLight, packedOverlay);
    }
}
