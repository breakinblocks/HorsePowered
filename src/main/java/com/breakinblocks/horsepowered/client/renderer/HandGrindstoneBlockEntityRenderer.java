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

/**
 * Renderer for the hand grindstone — displays the item currently being ground
 * on top of the grinding surface.
 */
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
        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
    }

    @Override
    public void submit(HandGrindstoneRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Block is 14/16 tall — sit the item just above that surface.
        RenderUtils.renderFlatItem(state.inputItem, poseStack, collector, state.lightCoords, 0.5D, 0.88D, 0.5D, 0.4F);
    }

    public static class HandGrindstoneRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
    }
}
