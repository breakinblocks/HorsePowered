package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.GraniteAnvilBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class GraniteAnvilBlockEntityRenderer implements BlockEntityRenderer<GraniteAnvilBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public GraniteAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(GraniteAnvilBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                be.getItem(0), 0.5, 0.84, 0.5, 1.375F, 0F, packedLight, packedOverlay,
                be.getLevel(), 1.2);
    }
}
