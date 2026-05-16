package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class GeneratorBlockEntityRenderer implements BlockEntityRenderer<GeneratorBlockEntity> {

    public GeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);
        VirtualWorkerRenderer.renderWorker(blockEntity, partialTick, poseStack, bufferSource, packedLight);
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);

        float gearYRot = Mth.rotLerp(partialTick, blockEntity.getPrevVirtualYRot(), blockEntity.getVirtualYRot());
        GearRenderer.renderGear(poseStack, bufferSource, gearYRot, packedLight);
    }

    @Override
    public boolean shouldRenderOffScreen(GeneratorBlockEntity blockEntity) {
        return blockEntity.shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(GeneratorBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3,
                        pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }
}
