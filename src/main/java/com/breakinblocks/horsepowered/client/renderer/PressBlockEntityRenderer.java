package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the horse-powered press.
 * Renders the leash between the block and the attached worker mob.
 */
public class PressBlockEntityRenderer implements BlockEntityRenderer<PressBlockEntity, PressBlockEntityRenderer.PressRenderState> {

    public PressBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public PressRenderState createRenderState() {
        return new PressRenderState();
    }

    @Override
    public void extractRenderState(PressBlockEntity blockEntity, PressRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        HorseBlockRenderState.extractWorkerState(blockEntity, state, partialTick);

        // Extract plunger animation progress
        state.visualProgress = blockEntity.getVisualProgress();

        // Extract fluid state
        state.fluidStack = blockEntity.getTank().getFluid().copy();
        state.tankCapacity = blockEntity.getTank().getCapacity();

        // Extract item states for rendering
        RenderUtils.extractItemState(state.inputItem, blockEntity.getItem(0), blockEntity.getLevel());
        RenderUtils.extractItemState(state.outputItem, blockEntity.getItem(1), blockEntity.getLevel());
    }

    @Override
    public void submit(PressRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        HorseBlockRenderState.submitWorkerAndArea(state, poseStack, collector, camera);

        // Render plunger
        PlungerRenderer.renderPlunger(poseStack, collector, state.visualProgress, state.lightCoords);

        // Render input item inside the press basin
        RenderUtils.renderFlatItem(state.inputItem, poseStack, collector, state.lightCoords, 0.5D, 0.15D, 0.5D, 0.5F);

        // Render output item beside the press
        RenderUtils.renderFlatItem(state.outputItem, poseStack, collector, state.lightCoords, 0.5D, 0.2D, 1.1D, 0.3F);

        // Render fluid in tank
        if (!state.fluidStack.isEmpty()) {
            FluidRenderer.renderFluid(poseStack, collector, state.fluidStack, state.tankCapacity, state.lightCoords);
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(PressBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3,
                        pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }

    public static class PressRenderState extends HorseBlockRenderState {
        public float visualProgress;
        public FluidStack fluidStack = FluidStack.EMPTY;
        public int tankCapacity;
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
    }
}
