package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renders a lead (rope) between a horse-powered block and its attached worker mob.
 * Uses similar technique to vanilla mob leash rendering.
 */
public class LeadRenderer {

    /**
     * Renders a lead from the block entity to its worker if present.
     *
     * @param blockEntity   The horse-powered block entity
     * @param partialTick   Partial tick for smooth rendering
     * @param poseStack     The pose stack
     * @param bufferSource  The buffer source
     */
    public static void renderLead(HPBlockEntityHorseBase blockEntity, float partialTick,
                                   PoseStack poseStack, MultiBufferSource bufferSource) {
        PathfinderMob worker = blockEntity.getWorker();
        if (worker == null || !blockEntity.hasWorkerForDisplay()) {
            return;
        }

        // Calculate block attach point (center of block, slightly above)
        BlockPos blockPos = blockEntity.getBlockPos();
        double blockX = 0.5; // Center of block relative to block position
        double blockY = 1.0; // Slightly above the block
        double blockZ = 0.5;

        // Calculate worker position (interpolated for smooth rendering)
        Vec3 workerPos = worker.getRopeHoldPosition(partialTick);

        // Offset from block entity position
        double dx = workerPos.x - blockPos.getX() - blockX;
        double dy = workerPos.y - blockPos.getY() - blockY;
        double dz = workerPos.z - blockPos.getZ() - blockZ;

        // Get light levels
        int blockLight = worker.level().getBrightness(LightLayer.BLOCK, blockEntity.getBlockPos().above());
        int skyLight = worker.level().getBrightness(LightLayer.SKY, blockEntity.getBlockPos().above());
        int workerBlockLight = worker.level().getBrightness(LightLayer.BLOCK, worker.blockPosition());
        int workerSkyLight = worker.level().getBrightness(LightLayer.SKY, worker.blockPosition());

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

        // Calculate rotation
        float yaw = (float)(Mth.atan2(dz, dx));
        float pitch = (float)(Mth.atan2(dy, horizontalDist));

        // Lead color (brownish like vanilla)
        float r1 = 0.553F;
        float g1 = 0.380F;
        float b1 = 0.243F;
        // Darker shadow color
        float r2 = 0.353F;
        float g2 = 0.180F;
        float b2 = 0.043F;

        int segments = 24;
        float segmentLength = totalDist / segments;

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
            vertexConsumer.vertex(matrix, x1 - width, y1, z1).color(r1, g1, b1, 1.0F).uv2(light1).endVertex();
            vertexConsumer.vertex(matrix, x2 - width, y2, z2).color(r1, g1, b1, 1.0F).uv2(light2).endVertex();
            vertexConsumer.vertex(matrix, x2 + width, y2, z2).color(r1, g1, b1, 1.0F).uv2(light2).endVertex();
            vertexConsumer.vertex(matrix, x1 + width, y1, z1).color(r1, g1, b1, 1.0F).uv2(light1).endVertex();

            // Bottom surface (darker)
            vertexConsumer.vertex(matrix, x1 - width, y1 - width, z1).color(r2, g2, b2, 1.0F).uv2(light1).endVertex();
            vertexConsumer.vertex(matrix, x2 - width, y2 - width, z2).color(r2, g2, b2, 1.0F).uv2(light2).endVertex();
            vertexConsumer.vertex(matrix, x2 + width, y2 - width, z2).color(r2, g2, b2, 1.0F).uv2(light2).endVertex();
            vertexConsumer.vertex(matrix, x1 + width, y1 - width, z1).color(r2, g2, b2, 1.0F).uv2(light1).endVertex();
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
