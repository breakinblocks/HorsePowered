package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Common rendering utilities shared across block entity renderers.
 */
public final class RenderUtils {

    private RenderUtils() {
        // Utility class - no instantiation
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
    public static void renderItemCountBillboard(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                                int packedLight, int count, double x, double y, double z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Billboard rotation - face the camera
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        // Scale down for text
        poseStack.scale(0.02F, -0.02F, 0.02F);

        String text = String.valueOf(count);
        float textX = -font.width(text) / 2.0F;

        Matrix4f matrix = poseStack.last().pose();

        // Draw text with background for visibility
        font.drawInBatch(text, textX, 0, 0xFFFFFF, true, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0x40000000, packedLight);

        poseStack.popPose();
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
