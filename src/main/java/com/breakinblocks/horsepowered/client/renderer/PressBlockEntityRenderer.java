package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class PressBlockEntityRenderer implements BlockEntityRenderer<PressBlockEntity> {

    private static final float PLUNGER_MAX_Y = 0.7F;
    private static final float PLUNGER_MIN_Y = 0.2F;

    private final ItemRenderer itemRenderer;
    private final Font font;

    public PressBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(PressBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);
        renderPlunger(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(0), 0.5, 0.35, 0.5, 0.5F, 0, packedLight, packedOverlay,
                blockEntity.getLevel(), 0.65);

        RenderUtils.renderFlatItem(poseStack, bufferSource, itemRenderer, font,
                blockEntity.getItem(1), 0.5, 0.3, 0.5, 0.35F, 0, packedLight, packedOverlay,
                blockEntity.getLevel(), 0.55);

        FluidStack inputFluid = blockEntity.getInputTank().getFluid();
        if (!inputFluid.isEmpty()) {
            renderFluidHalf(poseStack, bufferSource, packedLight, inputFluid, blockEntity.getInputTank().getCapacity(), true);
        }
        FluidStack outputFluid = blockEntity.getOutputTank().getFluid();
        if (!outputFluid.isEmpty()) {
            renderFluidHalf(poseStack, bufferSource, packedLight, outputFluid, blockEntity.getOutputTank().getCapacity(), false);
        }
    }

    private void renderPlunger(PressBlockEntity blockEntity, PoseStack poseStack,
                               MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int currentPress = blockEntity.getCurrentPressStatus();
        int totalPress = Configs.pointsForPress.get();
        if (totalPress <= 0) totalPress = 1;

        float progress = (float) currentPress / totalPress;
        float plungerTravel = PLUNGER_MAX_Y - PLUNGER_MIN_Y;
        float plungerY = PLUNGER_MAX_Y - (progress * plungerTravel);

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(Blocks.OAK_PLANKS.defaultBlockState())
                .getParticleIcon();

        poseStack.pushPose();

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        float plungerMinX = 0.15F;
        float plungerMaxX = 0.85F;
        float plungerMinZ = 0.15F;
        float plungerMaxZ = 0.85F;
        float plungerHeight = 0.15F;

        RenderUtils.renderTexturedBox(poseStack, builder, sprite, packedLight,
                plungerMinX, plungerY, plungerMinZ,
                plungerMaxX, plungerY + plungerHeight, plungerMaxZ);

        poseStack.popPose();
    }

    private void renderFluidHalf(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                 FluidStack fluidStack, int capacity, boolean leftHalf) {
        if (fluidStack.isEmpty() || capacity <= 0) return;

        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int color = fluidTypeExtensions.getTintColor(fluidStack);

        float fillPercentage = (float) fluidStack.getAmount() / capacity;
        float fluidHeight = 0.1F + (fillPercentage * 0.6F);

        poseStack.pushPose();
        float xOffset = leftHalf ? 0f : 0.375f;
        poseStack.translate(0.125 + xOffset, 0.1, 0.125);

        VertexConsumer builder = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = ((color >> 24) & 0xFF) / 255.0F;
        if (a == 0) a = 1.0F;

        float width = 0.375F;
        float depth = 0.75F;
        float u1 = sprite.getU0();
        float u2 = sprite.getU1();
        float v1 = sprite.getV0();
        float v2 = sprite.getV1();

        builder.vertex(matrix, 0, fluidHeight, 0).color(r, g, b, a).uv(u1, v1).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, 0, fluidHeight, depth).color(r, g, b, a).uv(u1, v2).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, width, fluidHeight, depth).color(r, g, b, a).uv(u2, v2).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, width, fluidHeight, 0).color(r, g, b, a).uv(u2, v1).uv2(packedLight).normal(0, 1, 0).endVertex();

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
