package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

/**
 * Common rendering utilities shared across block entity renderers.
 * TODO: Update for 1.21.11 render system changes
 */
public final class RenderUtils {

    private RenderUtils() {
        // Utility class - no instantiation
    }

    /**
     * Renders item count as a billboard that always faces the camera, like name tags.
     * TODO: Update for 1.21.11 render system - cameraOrientation() no longer available
     */
    public static void renderItemCountBillboard(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                                int packedLight, int count, double x, double y, double z) {
        // Disabled until new render system is understood
        // The cameraOrientation() method is no longer available on EntityRenderDispatcher
    }

    /**
     * Renders an item flat (rotated 90 degrees around X) at the given position and scale.
     * Handles push/translate/scale/rotate/submit/pop.
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
     * Adds a vertex to the buffer with all required attributes.
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
     * TODO: Update for 1.21.11 render system - getTextureAtlas() and RenderTypes.solid() changed
     */
    public static void renderTexturedBox(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Identifier textureLocation,
                                         float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ,
                                         int packedLight, int packedOverlay) {
        // Disabled until new render system is understood
        // getTextureAtlas() and RenderTypes.solid() are no longer available
    }
}
