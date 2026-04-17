package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
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

/**
 * Renderer for the hand chopping block — displays the log currently being chopped
 * on top of the station surface.
 */
public class ManualChopperBlockEntityRenderer implements BlockEntityRenderer<ManualChopperBlockEntity, ManualChopperBlockEntityRenderer.ManualChopperRenderState> {

    public ManualChopperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ManualChopperRenderState createRenderState() {
        return new ManualChopperRenderState();
    }

    @Override
    public void extractRenderState(ManualChopperBlockEntity blockEntity, ManualChopperRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
    }

    @Override
    public void submit(ManualChopperRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Block is 9/16 tall — sit the item just above that surface.
        RenderUtils.renderFlatItem(state.inputItem, poseStack, collector, state.lightCoords, 0.5D, 0.57D, 0.5D, 0.5F);
    }

    public static class ManualChopperRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
    }
}
