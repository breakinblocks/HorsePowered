package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;

/**
 * Shared rendering utilities for block entity renderers.
 * Consolidates common rendering code to follow DRY principles.
 */
public final class RenderUtils {

    private RenderUtils() {
        // Utility class - no instantiation
    }

    /**
     * Renders an item count as billboard text that always faces the camera.
     *
     * @param poseStack    The pose stack for transformations
     * @param bufferSource The buffer source for rendering
     * @param font         The font to use for text rendering
     * @param packedLight  The packed light value
     * @param count        The item count to display
     * @param x            X offset from block entity position
     * @param y            Y offset from block entity position
     * @param z            Z offset from block entity position
     */
    public static void renderItemCountBillboard(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                                int packedLight, int count, double x, double y, double z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Billboard effect - face the camera
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.02F, -0.02F, 0.02F);

        String text = String.valueOf(count);
        float textX = -font.width(text) / 2.0F;

        Matrix4f matrix = poseStack.last().pose();

        // Draw with shadow/background for visibility
        font.drawInBatch(text, textX, 0, 0xFFFFFF, true, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0x40000000, packedLight);

        poseStack.popPose();
    }

    /**
     * Adds a vertex to a vertex consumer with all necessary attributes.
     * Helper method for rendering textured quads.
     *
     * @param builder       The vertex consumer
     * @param pose          The transformation matrix
     * @param x             X position
     * @param y             Y position
     * @param z             Z position
     * @param u             Texture U coordinate
     * @param v             Texture V coordinate
     * @param nx            Normal X component
     * @param ny            Normal Y component
     * @param nz            Normal Z component
     * @param packedLight   The packed light value
     */
    public static void addVertex(VertexConsumer builder, Matrix4f pose,
                                 float x, float y, float z, float u, float v,
                                 float nx, float ny, float nz, int packedLight) {
        builder.vertex(pose, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .uv2(packedLight)
                .normal(nx, ny, nz)
                .endVertex();
    }

    /**
     * Renders a textured box (cuboid) at the specified position.
     * Useful for rendering animated elements like press plungers or chopper blades.
     *
     * @param poseStack     The pose stack for transformations
     * @param builder       The vertex consumer
     * @param sprite        The texture sprite to use
     * @param packedLight   The packed light value
     * @param minX          Minimum X coordinate (0-1 block scale)
     * @param minY          Minimum Y coordinate (0-1 block scale)
     * @param minZ          Minimum Z coordinate (0-1 block scale)
     * @param maxX          Maximum X coordinate (0-1 block scale)
     * @param maxY          Maximum Y coordinate (0-1 block scale)
     * @param maxZ          Maximum Z coordinate (0-1 block scale)
     */
    public static void renderTexturedBox(PoseStack poseStack, VertexConsumer builder, TextureAtlasSprite sprite,
                                         int packedLight, float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ) {
        Matrix4f pose = poseStack.last().pose();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face (Y+)
        addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight);
        addVertex(builder, pose, minX, maxY, maxZ, u0, v1, 0, 1, 0, packedLight);
        addVertex(builder, pose, maxX, maxY, maxZ, u1, v1, 0, 1, 0, packedLight);
        addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 1, 0, packedLight);

        // Bottom face (Y-)
        addVertex(builder, pose, minX, minY, maxZ, u0, v0, 0, -1, 0, packedLight);
        addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, -1, 0, packedLight);
        addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, -1, 0, packedLight);
        addVertex(builder, pose, maxX, minY, maxZ, u1, v0, 0, -1, 0, packedLight);

        // North face (Z-)
        addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight);
        addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, packedLight);
        addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, packedLight);
        addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, 0, -1, packedLight);

        // South face (Z+)
        addVertex(builder, pose, maxX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight);
        addVertex(builder, pose, minX, maxY, maxZ, u1, v0, 0, 0, 1, packedLight);
        addVertex(builder, pose, minX, minY, maxZ, u1, v1, 0, 0, 1, packedLight);
        addVertex(builder, pose, maxX, minY, maxZ, u0, v1, 0, 0, 1, packedLight);

        // West face (X-)
        addVertex(builder, pose, minX, maxY, maxZ, u0, v0, -1, 0, 0, packedLight);
        addVertex(builder, pose, minX, maxY, minZ, u1, v0, -1, 0, 0, packedLight);
        addVertex(builder, pose, minX, minY, minZ, u1, v1, -1, 0, 0, packedLight);
        addVertex(builder, pose, minX, minY, maxZ, u0, v1, -1, 0, 0, packedLight);

        // East face (X+)
        addVertex(builder, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight);
        addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, packedLight);
        addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, packedLight);
        addVertex(builder, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, packedLight);
    }
}
