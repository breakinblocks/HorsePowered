package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class HorseBlockRenderState extends BlockEntityRenderState {
    public float partialTick;
    public boolean hasWorker;
    public BlockPos blockPos;
    public boolean showHighlight;
    public List<Map.Entry<BlockPos, Boolean>> workingAreaPositions;

    public boolean showCounts;
    public double distanceToCameraSq;

    public Vec3 workerOffset;

    public Entity renderEntity;
    public double entityOffsetX, entityOffsetY, entityOffsetZ;
    public float entityYRot;

    public static void extractWorkerState(HPBlockEntityHorseBase blockEntity, HorseBlockRenderState state, float partialTick) {
        state.partialTick = partialTick;
        state.blockPos = blockEntity.getBlockPos();

        if (blockEntity.hasWorker()) {
            state.hasWorker = true;

            // Interpolate virtual position for smooth rendering
            double interpX = lerp(partialTick, blockEntity.getPrevVirtualX(), blockEntity.getVirtualX());
            double interpZ = lerp(partialTick, blockEntity.getPrevVirtualZ(), blockEntity.getVirtualZ());
            double interpY = blockEntity.getVirtualY();
            float interpYRot = lerpAngle(partialTick, blockEntity.getPrevVirtualYRot(), blockEntity.getVirtualYRot());

            // Leash attachment point: top of entity, relative to block center
            double blockCenterX = blockEntity.getBlockPos().getX() + 0.5;
            double blockCenterY = blockEntity.getBlockPos().getY();
            double blockCenterZ = blockEntity.getBlockPos().getZ() + 0.5;

            float entityHeight = blockEntity.getWorkerEntityHeight();
            state.workerOffset = new Vec3(
                    interpX - blockCenterX,
                    interpY + entityHeight * 0.7 - blockCenterY, // rope attaches ~70% up the entity
                    interpZ - blockCenterZ
            );

            // Entity rendering offset from block origin
            state.entityOffsetX = interpX - blockEntity.getBlockPos().getX();
            state.entityOffsetY = interpY - blockEntity.getBlockPos().getY();
            state.entityOffsetZ = interpZ - blockEntity.getBlockPos().getZ();
            state.entityYRot = interpYRot;

            // Get the cached render entity
            state.renderEntity = blockEntity.getCachedRenderEntity();
        } else {
            state.hasWorker = false;
            state.workerOffset = null;
            state.renderEntity = null;
        }

        state.showHighlight = blockEntity.shouldShowHighlight();
        state.workingAreaPositions = blockEntity.getWorkingAreaPositions();
    }

    public static void submitWorkerAndArea(HorseBlockRenderState state, PoseStack poseStack,
                                            SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.hasWorker) {
            if (state.renderEntity != null) {
                VirtualWorkerRenderer.renderEntity(
                        state.renderEntity,
                        state.entityOffsetX, state.entityOffsetY, state.entityOffsetZ,
                        state.entityYRot, state.partialTick,
                        poseStack, collector, camera, state.lightCoords);
            }

            if (state.workerOffset != null) {
                Vec3 blockAttachment = new Vec3(0.5, 1.0, 0.5);
                Vec3 workerPos = state.workerOffset.add(0.5, 0, 0.5);
                LeadRenderer.renderLead(blockAttachment, workerPos, poseStack, collector);
            }
        }

        WorkingAreaRenderer.render(state.showHighlight, state.workingAreaPositions,
                state.blockPos, poseStack, collector);
    }

    private static double lerp(float t, double a, double b) {
        return a + (b - a) * t;
    }

    private static float lerpAngle(float t, float a, float b) {
        float diff = b - a;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        return a + diff * t;
    }
}
