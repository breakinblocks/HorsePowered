package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockHandGrindstone;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HandGrindstoneBlockEntityRenderer implements BlockEntityRenderer<HandGrindstoneBlockEntity, HandGrindstoneBlockEntityRenderer.HandGrindstoneRenderState> {

    private static final double SIDE_Y = 0.3;
    private static final float SIDE_SCALE = 0.3F;

    public HandGrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public HandGrindstoneRenderState createRenderState() {
        return new HandGrindstoneRenderState();
    }

    @Override
    public void extractRenderState(HandGrindstoneBlockEntity blockEntity, HandGrindstoneRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.visibleRotation = blockEntity.getVisibleRotation();
        BlockState blockState = blockEntity.getBlockState();
        Direction front = blockState.hasProperty(BlockHandGrindstone.FACING)
                ? blockState.getValue(BlockHandGrindstone.FACING)
                : Direction.NORTH;
        state.inputSide = front.getClockWise();
        state.outputSide = front.getCounterClockWise();
        state.secondarySide = front;
        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
        RenderUtils.extractItemState(state.inputSideItem, blockEntity.getItem(0), blockEntity.getLevel());
        RenderUtils.extractItemState(state.outputItem, blockEntity.getItem(1), blockEntity.getLevel());
        RenderUtils.extractItemState(state.secondaryItem, blockEntity.getItem(2), blockEntity.getLevel());

        state.showCounts = RenderUtils.shouldShowItemCounts(blockEntity.getBlockPos());
        state.distanceToCameraSq = RenderUtils.distanceToCameraSq(blockEntity.getBlockPos(), cameraPos);
        state.inputCount = blockEntity.getItem(0).getCount();
        state.outputCount = blockEntity.getItem(1).getCount();
        state.secondaryCount = blockEntity.getItem(2).getCount();
    }

    @Override
    public void submit(HandGrindstoneRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderUtils.renderFlatItem(state.inputItem, poseStack, collector, state.lightCoords,
                0.5D, 0.88D, 0.5D, 0.4F, state.visibleRotation);
        submitSide(state.inputSideItem, state.inputSide, poseStack, collector, state.lightCoords);
        submitSide(state.outputItem, state.outputSide, poseStack, collector, state.lightCoords);
        submitSide(state.secondaryItem, state.secondarySide, poseStack, collector, state.lightCoords);

        if (state.showCounts) {
            RenderUtils.submitItemCount(state.inputCount, poseStack, collector, state.lightCoords,
                    state.distanceToCameraSq, camera, 0.5D, 1.2D, 0.5D);
            submitSideCount(state.outputCount, state.outputSide, poseStack, collector, state, camera);
            submitSideCount(state.secondaryCount, state.secondarySide, poseStack, collector, state, camera);
        }
    }

    private void submitSideCount(int count, Direction side, PoseStack poseStack, SubmitNodeCollector collector,
                                 HandGrindstoneRenderState state, CameraRenderState camera) {
        double x = 0.5 + side.getStepX() * 0.25;
        double z = 0.5 + side.getStepZ() * 0.25;
        RenderUtils.submitItemCount(count, poseStack, collector, state.lightCoords,
                state.distanceToCameraSq, camera, x, SIDE_Y + 0.3D, z);
    }

    private void submitSide(ItemStackRenderState item, Direction side, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        double x = 0.5 + side.getStepX() * 0.25;
        double z = 0.5 + side.getStepZ() * 0.25;
        RenderUtils.renderFlatItem(item, poseStack, collector, light, x, SIDE_Y, z, SIDE_SCALE);
    }

    public static class HandGrindstoneRenderState extends BlockEntityRenderState {
        public float visibleRotation;
        public Direction inputSide = Direction.NORTH;
        public Direction outputSide = Direction.NORTH;
        public Direction secondarySide = Direction.NORTH;
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState inputSideItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public final ItemStackRenderState secondaryItem = new ItemStackRenderState();
        public boolean showCounts;
        public double distanceToCameraSq;
        public int inputCount;
        public int outputCount;
        public int secondaryCount;
    }
}
