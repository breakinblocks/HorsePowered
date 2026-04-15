package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;

public class GearRenderer {

    private static final float HUB_MIN = 6f / 16f;
    private static final float HUB_MAX = 10f / 16f;
    private static final float GEAR_BOTTOM = 10f / 16f;
    private static final float GEAR_TOP = 12f / 16f;

    private static final float TOOTH_INNER = 3f / 16f;
    private static final float TOOTH_OUTER = 7.5f / 16f;
    private static final float TOOTH_HALF_THICK = 1f / 16f;

    private static final int TEETH = 8;

    public static void renderGear(PoseStack poseStack, SubmitNodeCollector collector, float yRotDegrees, int packedLight) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                .getBlockStateModelSet().getParticleMaterial(Blocks.POLISHED_DEEPSLATE.defaultBlockState()).sprite();

        collector.submitCustomGeometry(
                poseStack,
                Sheets.cutoutBlockSheet(),
                (pose, consumer) -> aabbBox(consumer, pose, sprite,
                        HUB_MIN, GEAR_BOTTOM, HUB_MIN,
                        HUB_MAX, GEAR_TOP, HUB_MAX,
                        packedLight)
        );

        for (int i = 0; i < TEETH; i++) {
            float toothAngle = yRotDegrees + i * (360f / TEETH);

            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(toothAngle));

            collector.submitCustomGeometry(
                    poseStack,
                    Sheets.cutoutBlockSheet(),
                    (pose, consumer) -> aabbBox(consumer, pose, sprite,
                            TOOTH_INNER, GEAR_BOTTOM, -TOOTH_HALF_THICK,
                            TOOTH_OUTER, GEAR_TOP,   TOOTH_HALF_THICK,
                            packedLight)
            );

            poseStack.popPose();
        }
    }

    private static void aabbBox(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                float minX, float minY, float minZ,
                                float maxX, float maxY, float maxZ,
                                int packedLight) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        int overlay = OverlayTexture.NO_OVERLAY;

        vx(consumer, pose, minX, maxY, maxZ, u0, v1, 0, 1, 0, packedLight, overlay);
        vx(consumer, pose, maxX, maxY, maxZ, u1, v1, 0, 1, 0, packedLight, overlay);
        vx(consumer, pose, maxX, maxY, minZ, u1, v0, 0, 1, 0, packedLight, overlay);
        vx(consumer, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight, overlay);

        vx(consumer, pose, minX, minY, minZ, u0, v0, 0, -1, 0, packedLight, overlay);
        vx(consumer, pose, maxX, minY, minZ, u1, v0, 0, -1, 0, packedLight, overlay);
        vx(consumer, pose, maxX, minY, maxZ, u1, v1, 0, -1, 0, packedLight, overlay);
        vx(consumer, pose, minX, minY, maxZ, u0, v1, 0, -1, 0, packedLight, overlay);

        vx(consumer, pose, minX, minY, minZ, u0, v1, 0, 0, -1, packedLight, overlay);
        vx(consumer, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight, overlay);
        vx(consumer, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, packedLight, overlay);
        vx(consumer, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, packedLight, overlay);

        vx(consumer, pose, maxX, minY, maxZ, u0, v1, 0, 0, 1, packedLight, overlay);
        vx(consumer, pose, maxX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight, overlay);
        vx(consumer, pose, minX, maxY, maxZ, u1, v0, 0, 0, 1, packedLight, overlay);
        vx(consumer, pose, minX, minY, maxZ, u1, v1, 0, 0, 1, packedLight, overlay);

        vx(consumer, pose, minX, minY, maxZ, u0, v1, -1, 0, 0, packedLight, overlay);
        vx(consumer, pose, minX, maxY, maxZ, u0, v0, -1, 0, 0, packedLight, overlay);
        vx(consumer, pose, minX, maxY, minZ, u1, v0, -1, 0, 0, packedLight, overlay);
        vx(consumer, pose, minX, minY, minZ, u1, v1, -1, 0, 0, packedLight, overlay);

        vx(consumer, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, packedLight, overlay);
        vx(consumer, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight, overlay);
        vx(consumer, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, packedLight, overlay);
        vx(consumer, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, packedLight, overlay);
    }

    private static void vx(VertexConsumer buf, PoseStack.Pose pose,
                           float x, float y, float z, float u, float v,
                           float nx, float ny, float nz, int light, int overlay) {
        buf.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
