package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChopperBlockEntityRenderer implements BlockEntityRenderer<ChopperBlockEntity, ChopperBlockEntityRenderer.ChopperRenderState> {

    public ChopperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ChopperRenderState createRenderState() {
        return new ChopperRenderState();
    }

    @Override
    public void extractRenderState(ChopperBlockEntity blockEntity, ChopperRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        // Skip the remaining state extraction when the BE is being destroyed — otherwise
        // item-model/texture lookups in the final frame can produce a purple/black flash.
        state.skipSubmit = blockEntity.isRemoved();
        if (state.skipSubmit) {
            state.inputItem.clear();
            state.outputItem.clear();
            state.hasWorker = false;
            state.showCounts = false;
            return;
        }
        HorseBlockRenderState.extractWorkerState(blockEntity, state, partialTick);

        state.visualWindup = blockEntity.getVisualWindup();

        BlockState blockState = blockEntity.getBlockState();
        state.facingRotation = blockState.hasProperty(BlockChopper.FACING)
                ? rotationFor(blockState.getValue(BlockChopper.FACING))
                : 0;

        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
        RenderUtils.extractItemState(state.outputItem, blockEntity.getItem(1), blockEntity.getLevel());

        state.showCounts = RenderUtils.shouldShowItemCounts(blockEntity.getBlockPos());
        state.distanceToCameraSq = RenderUtils.distanceToCameraSq(blockEntity.getBlockPos(), cameraPos);
        state.inputCount = blockEntity.getItem(0).getCount();
        state.outputCount = blockEntity.getItem(1).getCount();
    }

    private static float rotationFor(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> 270f;
            default -> 0f;
        };
    }

    @Override
    public void submit(ChopperRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.skipSubmit) return;

        HorseBlockRenderState.submitWorkerAndArea(state, poseStack, collector, camera);

        poseStack.pushPose();
        if (state.facingRotation != 0) {
            poseStack.translate(0.5F, 0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.facingRotation));
            poseStack.translate(-0.5F, 0F, -0.5F);
        }
        BladeRenderer.renderBlade(poseStack, collector, state.visualWindup, state.lightCoords);
        poseStack.popPose();

        RenderUtils.renderStandingItem(state.inputItem, poseStack, collector, state.lightCoords,
                0.5D, 0.6D, 0.5D, 0.6F);

        RenderUtils.renderFlatItem(state.outputItem, poseStack, collector, state.lightCoords,
                0.5D, 0.2D, 0.9D, 0.3F);

        if (state.showCounts) {
            RenderUtils.submitItemCount(state.inputCount, poseStack, collector, state.lightCoords,
                    state.distanceToCameraSq, camera, 0.5D, 1.15D, 0.5D);
            RenderUtils.submitItemCount(state.outputCount, poseStack, collector, state.lightCoords,
                    state.distanceToCameraSq, camera, 0.5D, 0.5D, 0.9D);
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(ChopperBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3,
                        pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }

    public static class ChopperRenderState extends HorseBlockRenderState {
        public boolean skipSubmit;
        public float visualWindup;
        public float facingRotation;
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public int inputCount;
        public int outputCount;
    }
}
