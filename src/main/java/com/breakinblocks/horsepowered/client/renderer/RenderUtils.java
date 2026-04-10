package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

/**
 * Common rendering utilities shared across block entity renderers.
 */
public final class RenderUtils {

    private RenderUtils() {
        // Utility class - no instantiation
    }

    /**
     * Renders an item flat (rotated 90 degrees around X) at the given position and scale.
     */
    public static void renderFlatItem(ItemStackRenderState itemState, PoseStack poseStack, SubmitNodeCollector collector,
                                      int lightCoords, double x, double y, double z, float scale) {
        if (itemState.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /**
     * Extracts item render state from an ItemStack, clearing the state if the stack is empty.
     */
    public static void extractItemState(ItemStackRenderState itemState, ItemStack stack, Level level) {
        if (!stack.isEmpty()) {
            Minecraft.getInstance().getItemModelResolver()
                    .updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, level, null, 0);
        } else {
            itemState.clear();
        }
    }

    /**
     * Adds a vertex to the buffer with all required attributes (Matrix4f variant).
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
     * Adds a vertex to the buffer with all required attributes (PoseStack.Pose variant).
     */
    public static void addVertex(VertexConsumer buffer, PoseStack.Pose pose,
                                 float x, float y, float z, float u, float v,
                                 float nx, float ny, float nz, int packedLight, int packedOverlay) {
        buffer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }

    /**
     * Renders a textured box using quads with proper UV coordinates scaled to face size.
     */
    public static void renderTexturedBox(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                         float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ,
                                         int packedLight) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Scale UVs based on face size
        float xSize = maxX - minX;
        float ySize = maxY - minY;
        float zSize = maxZ - minZ;

        float uXSize = u0 + (u1 - u0) * xSize;
        float uZSize = u0 + (u1 - u0) * zSize;
        float vYSize = v0 + (v1 - v0) * ySize;
        float vZSize = v0 + (v1 - v0) * zSize;

        int overlay = OverlayTexture.NO_OVERLAY;

        // Bottom face (y = minY)
        addVertex(buffer, pose, minX, minY, minZ, u0, v0, 0, -1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, minZ, uXSize, v0, 0, -1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, maxZ, uXSize, vZSize, 0, -1, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, minY, maxZ, u0, vZSize, 0, -1, 0, packedLight, overlay);

        // Top face (y = maxY)
        addVertex(buffer, pose, minX, maxY, maxZ, u0, vZSize, 0, 1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, maxZ, uXSize, vZSize, 0, 1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, minZ, uXSize, v0, 0, 1, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight, overlay);

        // North face (z = minZ)
        addVertex(buffer, pose, minX, minY, minZ, u0, vYSize, 0, 0, -1, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, minZ, uXSize, v0, 0, 0, -1, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, minZ, uXSize, vYSize, 0, 0, -1, packedLight, overlay);

        // South face (z = maxZ)
        addVertex(buffer, pose, maxX, minY, maxZ, u0, vYSize, 0, 0, 1, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, maxZ, uXSize, v0, 0, 0, 1, packedLight, overlay);
        addVertex(buffer, pose, minX, minY, maxZ, uXSize, vYSize, 0, 0, 1, packedLight, overlay);

        // West face (x = minX)
        addVertex(buffer, pose, minX, minY, maxZ, u0, vYSize, -1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, maxZ, u0, v0, -1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, minZ, uZSize, v0, -1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, minY, minZ, uZSize, vYSize, -1, 0, 0, packedLight, overlay);

        // East face (x = maxX)
        addVertex(buffer, pose, maxX, minY, minZ, u0, vYSize, 1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, maxZ, uZSize, v0, 1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, maxZ, uZSize, vYSize, 1, 0, 0, packedLight, overlay);
    }
}
