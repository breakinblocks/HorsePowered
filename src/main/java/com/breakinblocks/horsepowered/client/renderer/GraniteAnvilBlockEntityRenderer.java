package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GraniteAnvilBlockEntity;
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

public class GraniteAnvilBlockEntityRenderer implements BlockEntityRenderer<GraniteAnvilBlockEntity, GraniteAnvilBlockEntityRenderer.GraniteAnvilRenderState> {

    public GraniteAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public GraniteAnvilRenderState createRenderState() {
        return new GraniteAnvilRenderState();
    }

    @Override
    public void extractRenderState(GraniteAnvilBlockEntity blockEntity, GraniteAnvilRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
    }

    @Override
    public void submit(GraniteAnvilRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderUtils.renderFlatItem(state.inputItem, poseStack, collector, state.lightCoords,
                0.5D, 0.84D, 0.5D, 1.375F);
    }

    public static class GraniteAnvilRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
    }
}
