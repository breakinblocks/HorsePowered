package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlockEntityRenderer implements BlockEntityRenderer<GeneratorBlockEntity, GeneratorBlockEntityRenderer.GeneratorRenderState> {

    public GeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public GeneratorRenderState createRenderState() {
        return new GeneratorRenderState();
    }

    @Override
    public void extractRenderState(GeneratorBlockEntity blockEntity, GeneratorRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        HorseBlockRenderState.extractWorkerState(blockEntity, state, partialTick);
        if (state.hasWorker) {
            state.gearYRot = state.entityYRot;
        }
    }

    @Override
    public void submit(GeneratorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        HorseBlockRenderState.submitWorkerAndArea(state, poseStack, collector, camera);
        GearRenderer.renderGear(poseStack, collector, state.gearYRot, state.lightCoords);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(GeneratorBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3,
                        pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }

    public static class GeneratorRenderState extends HorseBlockRenderState {
        public float gearYRot;
    }
}
