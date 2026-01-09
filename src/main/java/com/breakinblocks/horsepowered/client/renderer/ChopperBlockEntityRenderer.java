package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Renderer for the horse-powered chopper.
 * Renders the leash between the block and the attached worker mob.
 */
public class ChopperBlockEntityRenderer implements BlockEntityRenderer<ChopperBlockEntity, ChopperBlockEntityRenderer.ChopperRenderState> {

    public ChopperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Context available for accessing rendering utilities
    }

    @Override
    public ChopperRenderState createRenderState() {
        return new ChopperRenderState();
    }

    @Override
    public void extractRenderState(ChopperBlockEntity blockEntity, ChopperRenderState state, float partialTick,
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

        // Extract blade animation value (for up/down chopping motion)
        state.visualWindup = blockEntity.getVisualWindup();

        // Extract working area highlight data
        state.showHighlight = blockEntity.shouldShowHighlight();
        state.workingAreaPositions = blockEntity.getWorkingAreaPositions();

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
    public void submit(ChopperRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
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

        // Render chopping blade (moves up and down based on windup)
        BladeRenderer.renderBlade(poseStack, collector, state.visualWindup, state.lightCoords);

        // Render input item on top of the oak base (base top is at Y = 6/16 = 0.375)
        if (!state.inputItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.44D, 0.5D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            state.inputItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render output item beside the base
        if (!state.outputItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.2D, 0.9D);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            state.outputItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    public static class ChopperRenderState extends BlockEntityRenderState {
        public float partialTick;
        public boolean hasWorker;
        public Vec3 workerOffset;  // Offset from block center to worker leash position
        public BlockPos blockPos;
        public boolean showHighlight;
        public List<Map.Entry<BlockPos, Boolean>> workingAreaPositions;
        public float visualWindup;  // Blade vertical position (-0.74 = down, 0 = up)

        // Item rendering states
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public int inputCount;
        public int outputCount;
    }
}
