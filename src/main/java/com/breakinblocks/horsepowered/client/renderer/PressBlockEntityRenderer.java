package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Renderer for the horse-powered press.
 * Renders the leash between the block and the attached worker mob.
 */
public class PressBlockEntityRenderer implements BlockEntityRenderer<PressBlockEntity, PressBlockEntityRenderer.PressRenderState> {

    public PressBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Context available for accessing rendering utilities
    }

    @Override
    public PressRenderState createRenderState() {
        return new PressRenderState();
    }

    @Override
    public void extractRenderState(PressBlockEntity blockEntity, PressRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.partialTick = partialTick;
        state.blockPos = blockEntity.getBlockPos();

        // Extract worker position for leash rendering
        PathfinderMob worker = blockEntity.getWorker();
        if (worker != null && worker.isAlive()) {
            // Get the leash holder position on the worker (near the neck)
            Vec3 workerWorldPos = worker.getRopeHoldPosition(partialTick);
            // Calculate offset from block center to worker (will be rendered relative to block origin)
            Vec3 blockWorldPos = Vec3.atCenterOf(blockEntity.getBlockPos());
            state.workerOffset = workerWorldPos.subtract(blockWorldPos);
            state.hasWorker = true;
        } else {
            state.workerOffset = null;
            state.hasWorker = false;
        }

        // Extract working area highlight data
        state.showHighlight = blockEntity.shouldShowHighlight();
        state.workingAreaPositions = blockEntity.getWorkingAreaPositions();

        // Extract plunger animation progress
        state.visualProgress = blockEntity.getVisualProgress();

        // Extract fluid state
        state.fluidStack = blockEntity.getTank().getFluid().copy();
        state.tankCapacity = blockEntity.getTank().getCapacity();

        // Extract item states for rendering
        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);

        state.inputCount = input.getCount();
        state.outputCount = output.getCount();

        var itemModelResolver = Minecraft.getInstance().getItemModelResolver();

        if (!input.isEmpty()) {
            itemModelResolver.updateForTopItem(state.inputItem, input, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        } else {
            state.inputItem.clear();
        }

        if (!output.isEmpty()) {
            itemModelResolver.updateForTopItem(state.outputItem, output, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        } else {
            state.outputItem.clear();
        }
    }

    @Override
    public void submit(PressRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Render the leash between block and worker
        // poseStack is already translated to block position by the rendering system
        if (state.hasWorker && state.workerOffset != null) {
            // Block attachment point is at (0.5, 1.0, 0.5) relative to block origin (center top)
            Vec3 blockAttachment = new Vec3(0.5, 1.0, 0.5);
            // Worker position is the offset from block center, plus adjustment for block attachment point
            Vec3 workerPos = state.workerOffset.add(0.5, 0, 0.5);

            LeadRenderer.renderLead(blockAttachment, workerPos, poseStack, collector);
        }

        // Render working area highlight
        WorkingAreaRenderer.render(state.showHighlight, state.workingAreaPositions,
                state.blockPos, poseStack, collector);

        // Render plunger (moves down based on progress)
        PlungerRenderer.renderPlunger(poseStack, collector, state.visualProgress, state.lightCoords);

        // Render input item inside the press basin (floor at Y = 1/16 = 0.0625)
        if (!state.inputItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.15D, 0.5D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            state.inputItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render output item beside the press
        if (!state.outputItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.2D, 1.1D);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            state.outputItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render fluid in tank
        if (!state.fluidStack.isEmpty()) {
            FluidRenderer.renderFluid(poseStack, collector, state.fluidStack, state.tankCapacity, state.lightCoords);
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    public static class PressRenderState extends BlockEntityRenderState {
        public float partialTick;
        public boolean hasWorker;
        public Vec3 workerOffset;  // Offset from block center to worker leash position
        public BlockPos blockPos;
        public boolean showHighlight;
        public List<Map.Entry<BlockPos, Boolean>> workingAreaPositions;

        // Plunger animation
        public float visualProgress;

        // Fluid rendering
        public FluidStack fluidStack = FluidStack.EMPTY;
        public int tankCapacity;

        // Item rendering states
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public int inputCount;
        public int outputCount;
    }
}
