package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.joml.Matrix4f;

public class PressBlockEntityRenderer implements BlockEntityRenderer<PressBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public PressBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(PressBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // Render working area highlight if active
        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);

        // Render lead to attached worker
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);
        FluidTank tank = blockEntity.getTank();

        // Render input items on the press plate
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.1D, 0.5D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            if (input.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0.0D, 0.0D, -0.4D);
                renderItemCount(poseStack, bufferSource, packedLight, input.getCount());
            }

            poseStack.popPose();
        }

        // Render output item
        if (!output.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.3D, 0.5D);
            poseStack.scale(0.35F, 0.35F, 0.35F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(output, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            if (output.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0.0D, 0.0D, -0.3D);
                renderItemCount(poseStack, bufferSource, packedLight, output.getCount());
            }

            poseStack.popPose();
        }

        // Render fluid in tank
        if (!tank.isEmpty()) {
            renderFluid(poseStack, bufferSource, packedLight, tank);
        }
    }

    private void renderFluid(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, FluidTank tank) {
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack.isEmpty()) return;

        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int color = fluidTypeExtensions.getTintColor(fluidStack);

        float fillPercentage = (float) tank.getFluidAmount() / tank.getCapacity();
        float fluidHeight = 0.1F + (fillPercentage * 0.6F);

        poseStack.pushPose();
        poseStack.translate(0.125, 0.1, 0.125);

        VertexConsumer builder = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = ((color >> 24) & 0xFF) / 255.0F;
        if (a == 0) a = 1.0F;

        float width = 0.75F;
        float u1 = sprite.getU0();
        float u2 = sprite.getU1();
        float v1 = sprite.getV0();
        float v2 = sprite.getV1();

        // Top face
        builder.addVertex(matrix, 0, fluidHeight, 0).setColor(r, g, b, a).setUv(u1, v1).setLight(packedLight).setNormal(0, 1, 0);
        builder.addVertex(matrix, 0, fluidHeight, width).setColor(r, g, b, a).setUv(u1, v2).setLight(packedLight).setNormal(0, 1, 0);
        builder.addVertex(matrix, width, fluidHeight, width).setColor(r, g, b, a).setUv(u2, v2).setLight(packedLight).setNormal(0, 1, 0);
        builder.addVertex(matrix, width, fluidHeight, 0).setColor(r, g, b, a).setUv(u2, v1).setLight(packedLight).setNormal(0, 1, 0);

        poseStack.popPose();
    }

    private void renderItemCount(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int count) {
        poseStack.pushPose();
        poseStack.scale(0.025F, -0.025F, 0.025F);

        String text = String.valueOf(count);
        float x = -font.width(text) / 2.0F;

        Matrix4f matrix = poseStack.last().pose();
        font.drawInBatch(text, x, 0, 0xFFFFFF, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(PressBlockEntity blockEntity) {
        return blockEntity.shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
