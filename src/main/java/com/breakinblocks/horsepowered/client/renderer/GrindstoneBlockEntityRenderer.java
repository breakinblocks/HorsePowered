package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
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
 * Renderer for the horse-powered grindstone.
 * Renders the leash between the block and the attached worker mob.
 */
public class GrindstoneBlockEntityRenderer implements BlockEntityRenderer<GrindstoneBlockEntity, GrindstoneBlockEntityRenderer.GrindstoneRenderState> {

    public GrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // ItemModelResolver is obtained from Minecraft.getInstance() in extractRenderState
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

        // Extract item states for rendering
        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);
        ItemStack secondary = blockEntity.getItem(2);

        state.inputCount = input.getCount();
        state.outputCount = output.getCount();
        state.secondaryCount = secondary.getCount();

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

        if (!secondary.isEmpty()) {
            itemModelResolver.updateForTopItem(state.secondaryItem, secondary, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        } else {
            state.secondaryItem.clear();
        }
    }

    @Override
    public void submit(GrindstoneRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
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

        // Render input item on top of the grinder (grinder top at Y = 8/16 = 0.5)
        if (!state.inputItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.55D, 0.5D);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            state.inputItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render output item in front of grindstone
        if (!state.outputItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.2D, -0.1D);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            state.outputItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render secondary output behind grindstone
        if (!state.secondaryItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.2D, 1.1D);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            state.secondaryItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    public static class GrindstoneRenderState extends BlockEntityRenderState {
        public float partialTick;
        public boolean hasWorker;
        public Vec3 workerOffset;  // Offset from block center to worker leash position
        public BlockPos blockPos;
        public boolean showHighlight;
        public List<Map.Entry<BlockPos, Boolean>> workingAreaPositions;

        // Item rendering states
        public final ItemStackRenderState inputItem = new ItemStackRenderState();
        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public final ItemStackRenderState secondaryItem = new ItemStackRenderState();
        public int inputCount;
        public int outputCount;
        public int secondaryCount;
    }
}
