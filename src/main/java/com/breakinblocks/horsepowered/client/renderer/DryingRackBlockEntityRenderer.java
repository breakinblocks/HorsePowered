package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity, DryingRackBlockEntityRenderer.DryingRackRenderState> {

    public DryingRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public DryingRackRenderState createRenderState() {
        return new DryingRackRenderState();
    }

    @Override
    public void extractRenderState(DryingRackBlockEntity be, DryingRackRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        state.facing = blockState.hasProperty(BlockDryingRack.FACING)
                ? blockState.getValue(BlockDryingRack.FACING)
                : Direction.NORTH;
        for (int slot = 0; slot < DryingRackBlockEntity.SLOT_COUNT; slot++) {
            RenderUtils.extractItemState(state.slots[slot], be.getItem(slot), be.getLevel());
        }
    }

    @Override
    public void submit(DryingRackRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Direction rightDir = state.facing.getClockWise();
        double y = 14.5 / 16.0;
        for (int slot = 0; slot < DryingRackBlockEntity.SLOT_COUNT; slot++) {
            ItemStackRenderState item = state.slots[slot];
            if (item.isEmpty()) continue;
            int row = slot / 2;
            int col = slot % 2;
            double[] rowCenters = { 4.0, 12.0, 20.0, 28.0 };
            double[] colCenters = { 10.0, 22.0 };
            double rightPx = colCenters[col];
            double forwardPx = rowCenters[row];
            double rightOffset = (rightPx / 16.0) - 0.5;
            double forwardOffset = (forwardPx / 16.0) - 0.5;
            double x = 0.5 + rightOffset * rightDir.getStepX() + forwardOffset * state.facing.getStepX();
            double z = 0.5 + rightOffset * rightDir.getStepZ() + forwardOffset * state.facing.getStepZ();
            RenderUtils.renderFlatItem(item, poseStack, collector, state.lightCoords, x, y, z, 0.45F);
        }
    }

    @Override
    public AABB getRenderBoundingBox(DryingRackBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();
        if (!state.hasProperty(BlockDryingRack.FACING)) {
            return new AABB(pos);
        }
        Direction facing = state.getValue(BlockDryingRack.FACING);
        Direction rightDir = facing.getClockWise();
        int minX = Math.min(0, Math.min(facing.getStepX(), rightDir.getStepX() + facing.getStepX()));
        int maxX = Math.max(0, Math.max(facing.getStepX(), rightDir.getStepX() + facing.getStepX()));
        int minZ = Math.min(0, Math.min(facing.getStepZ(), rightDir.getStepZ() + facing.getStepZ()));
        int maxZ = Math.max(0, Math.max(facing.getStepZ(), rightDir.getStepZ() + facing.getStepZ()));
        minX = Math.min(minX, rightDir.getStepX());
        maxX = Math.max(maxX, rightDir.getStepX());
        minZ = Math.min(minZ, rightDir.getStepZ());
        maxZ = Math.max(maxZ, rightDir.getStepZ());
        return new AABB(pos.getX() + minX, pos.getY(), pos.getZ() + minZ,
                pos.getX() + maxX + 1, pos.getY() + 1, pos.getZ() + maxZ + 1);
    }

    public static class DryingRackRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public final ItemStackRenderState[] slots = new ItemStackRenderState[DryingRackBlockEntity.SLOT_COUNT];

        public DryingRackRenderState() {
            for (int i = 0; i < slots.length; i++) {
                slots[i] = new ItemStackRenderState();
            }
        }
    }
}
