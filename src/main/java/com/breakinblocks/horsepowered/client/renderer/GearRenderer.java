package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class GearRenderer {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("block/polished_deepslate");

    private static final float HUB_MIN = 6f / 16f;
    private static final float HUB_MAX = 10f / 16f;
    private static final float GEAR_BOTTOM = 10f / 16f;
    private static final float GEAR_TOP = 12f / 16f;

    private static final float TOOTH_INNER = 3f / 16f;
    private static final float TOOTH_OUTER = 7.5f / 16f;
    private static final float TOOTH_HALF_THICK = 1f / 16f;

    private static final int TEETH = 8;

    public static void renderGear(PoseStack poseStack, MultiBufferSource bufferSource, float yRotDegrees, int packedLight) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TEXTURE);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.cutout());

        Matrix4f pose = poseStack.last().pose();
        box(builder, pose, sprite,
                HUB_MIN, GEAR_BOTTOM, HUB_MIN,
                HUB_MAX, GEAR_TOP, HUB_MAX,
                packedLight);

        for (int i = 0; i < TEETH; i++) {
            float toothAngle = yRotDegrees + i * (360f / TEETH);

            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(toothAngle));

            Matrix4f toothPose = poseStack.last().pose();
            box(builder, toothPose, sprite,
                    TOOTH_INNER, GEAR_BOTTOM, -TOOTH_HALF_THICK,
                    TOOTH_OUTER, GEAR_TOP,    TOOTH_HALF_THICK,
                    packedLight);

            poseStack.popPose();
        }
    }

    private static void box(VertexConsumer builder, Matrix4f pose, TextureAtlasSprite sprite,
                            float minX, float minY, float minZ,
                            float maxX, float maxY, float maxZ,
                            int packedLight) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        int overlay = OverlayTexture.NO_OVERLAY;

        // Top (Y+)
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u0, v1, 0, 1, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v1, 0, 1, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 1, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight, overlay);

        // Bottom (Y-)
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v0, 0, -1, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u1, v0, 0, -1, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 0, -1, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u0, v1, 0, -1, 0, packedLight, overlay);

        // North (Z-)
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, 0, -1, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, packedLight, overlay);

        // South (Z+)
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u0, v1, 0, 0, 1, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u1, v0, 0, 0, 1, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u1, v1, 0, 0, 1, packedLight, overlay);

        // West (X-)
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u0, v1, -1, 0, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u0, v0, -1, 0, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u1, v0, -1, 0, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u1, v1, -1, 0, 0, packedLight, overlay);

        // East (X+)
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, packedLight, overlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, packedLight, overlay);
    }
}
