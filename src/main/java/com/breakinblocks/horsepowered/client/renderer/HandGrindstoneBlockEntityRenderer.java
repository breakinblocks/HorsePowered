package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HandGrindstoneBlockEntityRenderer implements BlockEntityRenderer<HandGrindstoneBlockEntity, HandGrindstoneBlockEntityRenderer.HandGrindstoneRenderState> {

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
        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
        RenderUtils.extractItemState(state.outputItem, blockEntity.getItem(1), blockEntity.getLevel());
        RenderUtils.extractItemState(state.secondaryItem, blockEntity.getItem(2), blockEntity.getLevel());
    }

    @Override
    public void submit(HandGrindstoneRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Input spins with the grindstone so each click has visible feedback.
        RenderUtils.renderFlatItem(state.inputItem, poseStack, collector, state.lightCoords,
                0.5D, 0.88D, 0.5D, 0.4F, state.visibleRotation);
        RenderUtils.renderFlatItem(state.outputItem, poseStack, collector, state.lightCoords,
                0.5D, 0.3D, 0.25D, 0.3F);
        RenderUtils.renderFlatItem(state.secondaryItem, poseStack, collector, state.lightCoords,
                0.5D, 0.3D, 0.75D, 0.3F);
    }

    public static class HandGrindstoneRenderState extends BlockEntityRenderState {
        public float visibleRotation;
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public final ItemStackRenderState secondaryItem = new ItemStackRenderState();
    }
}
