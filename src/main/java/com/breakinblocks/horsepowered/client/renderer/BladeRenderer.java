package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;

/**
 * Renders the chopping blade for the horse-powered chopper.
 * The blade moves up and down like a guillotine based on the windup progress.
 * Uses the 1.21.9+ submitCustomGeometry API for rendering with iron block texture.
 */
public class BladeRenderer {

    // Blade dimensions from the model (in block units, converted from 1/16)
    // Original model: from [3, 23, 7.5] to [13, 26, 8.5]
    // We render relative to block origin, and the blade is above the block
    private static final float BLADE_MIN_X = 3f / 16f;
    private static final float BLADE_MAX_X = 13f / 16f;
    private static final float BLADE_MIN_Y = 23f / 16f;  // Above the block (at rest position)
    private static final float BLADE_MAX_Y = 26f / 16f;
    private static final float BLADE_MIN_Z = 7.5f / 16f;
    private static final float BLADE_MAX_Z = 8.5f / 16f;

    // How far the blade travels vertically (in blocks)
    // visualWindup ranges from -0.74 (down/chopped) to 0 (up/raised)
    // So we multiply by a scale to get actual block units
    private static final float BLADE_TRAVEL = 0.5f;  // Half a block of travel

    /**
     * Renders the chopping blade for the chopper.
     * The blade moves vertically based on the windup value.
     *
     * @param poseStack The pose stack (already translated to block position)
     * @param collector The node collector for submitting geometry
     * @param visualWindup The windup value (-0.74 = down/chopped, 0 = up/raised)
     * @param packedLight The packed light value for proper lighting
     */
    public static void renderBlade(PoseStack poseStack, SubmitNodeCollector collector, float visualWindup, int packedLight) {
        poseStack.pushPose();

        // Translate blade vertically based on windup
        // visualWindup of 0 = blade at rest (up), -0.74 = blade down (chopped)
        // We want positive visualWindup to raise the blade, negative to lower it
        float yOffset = visualWindup * BLADE_TRAVEL / 0.74f;
        poseStack.translate(0, yOffset, 0);

        // Get iron block texture sprite
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer()
                .getBlockModelShaper().getParticleIcon(Blocks.IRON_BLOCK.defaultBlockState(),
                        EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);

        // Submit custom geometry for the blade using the solid block sheet
        collector.submitCustomGeometry(
                poseStack,
                Sheets.solidBlockSheet(),
                (pose, consumer) -> {
                    renderTexturedBox(consumer, pose, sprite,
                            BLADE_MIN_X, BLADE_MIN_Y, BLADE_MIN_Z,
                            BLADE_MAX_X, BLADE_MAX_Y, BLADE_MAX_Z,
                            packedLight);
                }
        );

        poseStack.popPose();
    }

    /**
     * Renders a textured box using quads with proper UV coordinates.
     */
    private static void renderTexturedBox(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                           float minX, float minY, float minZ,
                                           float maxX, float maxY, float maxZ,
                                           int packedLight) {
        // Calculate UV coordinates from sprite
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Scale UVs based on face size (for proper texture tiling)
        float xSize = maxX - minX;
        float ySize = maxY - minY;
        float zSize = maxZ - minZ;

        // UV for different face sizes
        float uXSize = u0 + (u1 - u0) * xSize;
        float uZSize = u0 + (u1 - u0) * zSize;
        float vYSize = v0 + (v1 - v0) * ySize;
        float vZSize = v0 + (v1 - v0) * zSize;
        float vXSize = v0 + (v1 - v0) * xSize;

        int overlay = OverlayTexture.NO_OVERLAY;

        // Bottom face (y = minY) - X-Z plane
        addVertex(buffer, pose, minX, minY, minZ, u0, v0, 0, -1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, minZ, uXSize, v0, 0, -1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, maxZ, uXSize, vZSize, 0, -1, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, minY, maxZ, u0, vZSize, 0, -1, 0, packedLight, overlay);

        // Top face (y = maxY) - X-Z plane
        addVertex(buffer, pose, minX, maxY, maxZ, u0, vZSize, 0, 1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, maxZ, uXSize, vZSize, 0, 1, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, minZ, uXSize, v0, 0, 1, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight, overlay);

        // North face (z = minZ) - X-Y plane
        addVertex(buffer, pose, minX, minY, minZ, u0, vYSize, 0, 0, -1, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, minZ, uXSize, v0, 0, 0, -1, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, minZ, uXSize, vYSize, 0, 0, -1, packedLight, overlay);

        // South face (z = maxZ) - X-Y plane
        addVertex(buffer, pose, maxX, minY, maxZ, u0, vYSize, 0, 0, 1, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, maxZ, uXSize, v0, 0, 0, 1, packedLight, overlay);
        addVertex(buffer, pose, minX, minY, maxZ, uXSize, vYSize, 0, 0, 1, packedLight, overlay);

        // West face (x = minX) - Z-Y plane
        addVertex(buffer, pose, minX, minY, maxZ, u0, vYSize, -1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, maxZ, u0, v0, -1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, maxY, minZ, uZSize, v0, -1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, minX, minY, minZ, uZSize, vYSize, -1, 0, 0, packedLight, overlay);

        // East face (x = maxX) - Z-Y plane
        addVertex(buffer, pose, maxX, minY, minZ, u0, vYSize, 1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, maxY, maxZ, uZSize, v0, 1, 0, 0, packedLight, overlay);
        addVertex(buffer, pose, maxX, minY, maxZ, uZSize, vYSize, 1, 0, 0, packedLight, overlay);
    }

    private static void addVertex(VertexConsumer buffer, PoseStack.Pose pose,
                                   float x, float y, float z, float u, float v,
                                   float nx, float ny, float nz, int packedLight, int packedOverlay) {
        buffer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }
}
