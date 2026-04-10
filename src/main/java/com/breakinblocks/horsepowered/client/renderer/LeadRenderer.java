package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4f;

/**
 * Renders a lead (rope) between a horse-powered block and its attached virtual worker.
 * Uses similar technique to vanilla mob leash rendering.
 */
public class LeadRenderer {

    /**
     * Renders a lead from the block entity to its virtual worker if present.
     */
    public static void renderLead(HPBlockEntityHorseBase blockEntity, float partialTick,
                                   PoseStack poseStack, MultiBufferSource bufferSource) {
        if (!blockEntity.hasWorkerForDisplay()) {
            return;
        }
        Level level = blockEntity.getLevel();
        if (level == null) return;

        // Block attach point (center of block, slightly above)
        BlockPos blockPos = blockEntity.getBlockPos();
        double blockX = 0.5;
        double blockY = 1.0;
        double blockZ = 0.5;

        // Interpolated virtual worker position (smooth rendering)
        double workerX = Mth.lerp(partialTick, blockEntity.getPrevVirtualX(), blockEntity.getVirtualX());
        double workerZ = Mth.lerp(partialTick, blockEntity.getPrevVirtualZ(), blockEntity.getVirtualZ());
        double workerY = blockEntity.getVirtualY() + blockEntity.getWorkerEntityHeight() * 0.7;

        // Offset from block entity position
        double dx = workerX - blockPos.getX() - blockX;
        double dy = workerY - blockPos.getY() - blockY;
        double dz = workerZ - blockPos.getZ() - blockZ;

        // Get light levels
        BlockPos workerBlockPos = BlockPos.containing(workerX, workerY, workerZ);
        int blockLight = level.getBrightness(LightLayer.BLOCK, blockPos.above());
        int skyLight = level.getBrightness(LightLayer.SKY, blockPos.above());
        int workerBlockLight = level.getBrightness(LightLayer.BLOCK, workerBlockPos);
        int workerSkyLight = level.getBrightness(LightLayer.SKY, workerBlockPos);

        poseStack.pushPose();
        poseStack.translate(blockX, blockY, blockZ);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();

        // Render the lead segments (catenary curve)
        renderLeadSegments(matrix, vertexConsumer, dx, dy, dz,
                blockLight, skyLight, workerBlockLight, workerSkyLight);

        poseStack.popPose();
    }

    /**
     * Renders the lead as a series of connected segments forming a catenary curve.
     */
    private static void renderLeadSegments(Matrix4f matrix, VertexConsumer vertexConsumer,
                                            double dx, double dy, double dz,
                                            int blockLight, int skyLight,
                                            int workerBlockLight, int workerSkyLight) {
        float horizontalDist = Mth.sqrt((float)(dx * dx + dz * dz));
        float totalDist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));

        // Lead color (brownish like vanilla)
        float r1 = 0.553F;
        float g1 = 0.380F;
        float b1 = 0.243F;
        // Darker shadow color
        float r2 = 0.353F;
        float g2 = 0.180F;
        float b2 = 0.043F;

        int segments = 24;

        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            // Calculate positions along the rope with a sag
            float sag1 = calculateSag(t1, totalDist);
            float sag2 = calculateSag(t2, totalDist);

            float x1 = (float) (dx * t1);
            float y1 = (float) (dy * t1) - sag1;
            float z1 = (float) (dz * t1);

            float x2 = (float) (dx * t2);
            float y2 = (float) (dy * t2) - sag2;
            float z2 = (float) (dz * t2);

            // Interpolate light
            int light1 = LightTexture.pack(
                    (int) Mth.lerp(t1, blockLight, workerBlockLight),
                    (int) Mth.lerp(t1, skyLight, workerSkyLight));
            int light2 = LightTexture.pack(
                    (int) Mth.lerp(t2, blockLight, workerBlockLight),
                    (int) Mth.lerp(t2, skyLight, workerSkyLight));

            // Render quad for this segment (top surface)
            float width = 0.025F;

            // Top surface
            vertexConsumer.addVertex(matrix, x1 - width, y1, z1).setColor(r1, g1, b1, 1.0F).setLight(light1);
            vertexConsumer.addVertex(matrix, x2 - width, y2, z2).setColor(r1, g1, b1, 1.0F).setLight(light2);
            vertexConsumer.addVertex(matrix, x2 + width, y2, z2).setColor(r1, g1, b1, 1.0F).setLight(light2);
            vertexConsumer.addVertex(matrix, x1 + width, y1, z1).setColor(r1, g1, b1, 1.0F).setLight(light1);

            // Bottom surface (darker)
            vertexConsumer.addVertex(matrix, x1 - width, y1 - width, z1).setColor(r2, g2, b2, 1.0F).setLight(light1);
            vertexConsumer.addVertex(matrix, x2 - width, y2 - width, z2).setColor(r2, g2, b2, 1.0F).setLight(light2);
            vertexConsumer.addVertex(matrix, x2 + width, y2 - width, z2).setColor(r2, g2, b2, 1.0F).setLight(light2);
            vertexConsumer.addVertex(matrix, x1 + width, y1 - width, z1).setColor(r2, g2, b2, 1.0F).setLight(light1);
        }
    }

    /**
     * Calculate the sag of the rope at position t (0 to 1).
     * Creates a catenary-like curve.
     */
    private static float calculateSag(float t, float totalDist) {
        // Parabolic sag - maximum at middle, zero at ends
        float sagAmount = Math.min(totalDist * 0.1F, 0.5F); // Scale sag based on distance
        return 4.0F * sagAmount * t * (1.0F - t);
    }
}
