package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.joml.Matrix4f;

public class PressBlockEntityRenderer implements BlockEntityRenderer<PressBlockEntity> {

    // Plunger position constants - the plunger presses down into the basin
    private static final float PLUNGER_MAX_Y = 0.7F;     // Highest position (not pressing)
    private static final float PLUNGER_MIN_Y = 0.2F;     // Lowest position (fully pressed into basin)

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

        // Render animated plunger
        renderPlunger(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);

        // Render input items on the press plate
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.35D, 0.5D);  // Lower position - inside basin
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            poseStack.popPose();

            // Render count as billboard if more than 1
            if (input.getCount() > 1 && Configs.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight,
                        input.getCount(), 0.5D, 0.65D, 0.5D);
            }
        }

        // Render output item
        if (!output.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.3D, 0.5D);
            poseStack.scale(0.35F, 0.35F, 0.35F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(output, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            poseStack.popPose();

            // Render count as billboard if more than 1
            if (output.getCount() > 1 && Configs.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight,
                        output.getCount(), 0.5D, 0.55D, 0.5D);
            }
        }

        // Render fluid in tank
        if (!tank.isEmpty()) {
            renderFluid(poseStack, bufferSource, packedLight, tank);
        }
    }

    private void renderPlunger(PressBlockEntity blockEntity, PoseStack poseStack,
                               MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Get press progress from block entity
        int currentPress = blockEntity.getCurrentPressStatus();
        int totalPress = Configs.pointsForPress.get();
        if (totalPress <= 0) totalPress = 1;

        // Calculate plunger Y position based on progress
        float progress = (float) currentPress / totalPress;
        float plungerTravel = PLUNGER_MAX_Y - PLUNGER_MIN_Y;
        float plungerY = PLUNGER_MAX_Y - (progress * plungerTravel);

        // Get oak planks texture for plunger
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(Blocks.OAK_PLANKS.defaultBlockState())
                .getParticleIcon();

        poseStack.pushPose();

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        // Plunger dimensions (square plate that fits inside the basin)
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
        builder.vertex(matrix, 0, fluidHeight, 0).color(r, g, b, a).uv(u1, v1).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, 0, fluidHeight, width).color(r, g, b, a).uv(u1, v2).uv2(packedLight).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, width, fluidHeight, width).color(r, g, b, a).uv(u2, v2).uv2(packedLight).normal(0, 1, 0).endVertex();
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
