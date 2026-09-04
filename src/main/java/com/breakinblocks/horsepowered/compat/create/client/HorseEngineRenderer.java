package com.breakinblocks.horsepowered.compat.create.client;

import com.breakinblocks.horsepowered.client.renderer.LeadRenderer;
import com.breakinblocks.horsepowered.client.renderer.VirtualWorkerRenderer;
import com.breakinblocks.horsepowered.client.renderer.WorkingAreaRenderer;
import com.breakinblocks.horsepowered.compat.create.HorseEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class HorseEngineRenderer extends KineticBlockEntityRenderer<HorseEngineBlockEntity> {

    public HorseEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HorseEngineBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        WorkingAreaRenderer.renderIfActive(be, ms, buffer);
        VirtualWorkerRenderer.renderWorker(be, partialTicks, ms, buffer, light);
        LeadRenderer.renderLead(be, partialTicks, ms, buffer);
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(HorseEngineBlockEntity be, BlockState state) {
        return CachedBuffers.partial(CreateCompatClient.COG, state);
    }

    @Override
    public boolean shouldRenderOffScreen(HorseEngineBlockEntity be) {
        return be.getVirtualWorker().shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
