package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the horse-powered grindstone.
 * Renders the leash between the block and the attached worker mob.
 */
public class GrindstoneBlockEntityRenderer implements BlockEntityRenderer<GrindstoneBlockEntity, GrindstoneBlockEntityRenderer.GrindstoneRenderState> {

    public GrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public GrindstoneRenderState createRenderState() {
        return new GrindstoneRenderState();
    }

    @Override
    public void extractRenderState(GrindstoneBlockEntity blockEntity, GrindstoneRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        HorseBlockRenderState.extractWorkerState(blockEntity, state, partialTick);

        // Extract item states for rendering
        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
        RenderUtils.extractItemState(state.outputItem, blockEntity.getItem(1), blockEntity.getLevel());
        RenderUtils.extractItemState(state.secondaryItem, blockEntity.getItem(2), blockEntity.getLevel());
    }

    @Override
    public void submit(GrindstoneRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        HorseBlockRenderState.submitWorkerAndArea(state, poseStack, collector, camera);

        RenderUtils.renderFlatItem(state.inputItem, poseStack, collector, state.lightCoords, 0.5D, 0.55D, 0.5D, 0.4F);
        RenderUtils.renderFlatItem(state.outputItem, poseStack, collector, state.lightCoords, 0.5D, 0.2D, 0.18D, 0.3F);
        RenderUtils.renderFlatItem(state.secondaryItem, poseStack, collector, state.lightCoords, 0.5D, 0.2D, 0.82D, 0.3F);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(GrindstoneBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3,
                        pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }

    public static class GrindstoneRenderState extends HorseBlockRenderState {
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public final ItemStackRenderState secondaryItem = new ItemStackRenderState();
    }
}
