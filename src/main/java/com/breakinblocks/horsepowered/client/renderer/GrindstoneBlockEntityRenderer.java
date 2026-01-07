package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the horse-powered grindstone.
 * TODO: Implement proper item and lead rendering
 */
public class GrindstoneBlockEntityRenderer implements BlockEntityRenderer<GrindstoneBlockEntity, GrindstoneBlockEntityRenderer.GrindstoneRenderState> {

    public GrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Context available for accessing rendering utilities
    }

    @Override
    public GrindstoneRenderState createRenderState() {
        return new GrindstoneRenderState();
    }

    @Override
    public void extractRenderState(GrindstoneBlockEntity blockEntity, GrindstoneRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.partialTick = partialTick;
        // Extract any additional state needed for rendering
    }

    @Override
    public void submit(GrindstoneRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // TODO: Implement rendering with new 1.21.11 submit API
        // - Render input/output items
        // - Render lead to worker if attached
        // - Render working area highlight if active
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    public static class GrindstoneRenderState extends BlockEntityRenderState {
        public float partialTick;
    }
}
