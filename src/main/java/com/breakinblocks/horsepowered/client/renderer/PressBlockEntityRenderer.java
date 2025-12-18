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
import net.minecraft.client.renderer.texture.TextureAtlas;
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

    private static final ResourceLocation PLANK_TEXTURE = ResourceLocation.withDefaultNamespace("block/oak_planks");

    // Plunger dimensions (in blocks) - wide flat lid that presses down
    private static final float PLUNGER_WIDTH = 0.75F;    // 12/16 - wide to cover press area
    private static final float PLUNGER_DEPTH = 0.75F;    // 12/16 - deep to cover press area
    private static final float PLUNGER_HEIGHT = 0.125F;  // 2/16 - thin like a plank lid

    // Plunger position constants
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

        // Render the pressing plunger
        renderPlunger(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);
        FluidTank tank = blockEntity.getTank();

        // Render input items in the press basin
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.35D, 0.5D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);
            poseStack.popPose();

            if (input.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, input.getCount(), 0.5D, 0.65D, 0.5D);
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

            if (output.getCount() > 1 && HorsePowerConfig.renderItemAmount.get()) {
                RenderUtils.renderItemCountBillboard(poseStack, bufferSource, font, packedLight, output.getCount(), 0.5D, 0.55D, 0.5D);
            }
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

    @SuppressWarnings("deprecation")
    private void renderPlunger(PressBlockEntity blockEntity, PoseStack poseStack,
                               MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Get the visual progress (0.0 to 1.0, where 1.0 is fully pressed)
        float progress = blockEntity.getVisualProgress();

        // Plunger moves DOWN as progress increases (opposite of chopper blade)
        // At progress 0: plunger at MAX_Y (raised)
        // At progress 1: plunger at MIN_Y (pressed down)
        float plungerY = PLUNGER_MAX_Y - (PLUNGER_MAX_Y - PLUNGER_MIN_Y) * progress;

        // Get oak planks texture from block atlas
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(PLANK_TEXTURE);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        poseStack.pushPose();

        // Center the plunger
        float halfWidth = PLUNGER_WIDTH / 2;
        float halfDepth = PLUNGER_DEPTH / 2;

        float minX = 0.5F - halfWidth;
        float maxX = 0.5F + halfWidth;
        float minY = plungerY;
        float maxY = plungerY + PLUNGER_HEIGHT;
        float minZ = 0.5F - halfDepth;
        float maxZ = 0.5F + halfDepth;

        Matrix4f pose = poseStack.last().pose();

        // Get UV coordinates for the full texture
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face (Y+)
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u0, v1, 0, 1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v1, 0, 1, 0, packedLight, packedOverlay);

        // Bottom face (Y-)
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v0, 0, -1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u1, v0, 0, -1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 0, -1, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u0, v1, 0, -1, 0, packedLight, packedOverlay);

        // North face (Z-)
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, 0, -1, packedLight, packedOverlay);

        // South face (Z+)
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 0, 0, 1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u0, v1, 0, 0, 1, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 0, 0, 1, packedLight, packedOverlay);

        // West face (X-)
        RenderUtils.addVertex(builder, pose, minX, maxY, maxZ, u1, v0, -1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, maxY, minZ, u0, v0, -1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, minZ, u0, v1, -1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, minX, minY, maxZ, u1, v1, -1, 0, 0, packedLight, packedOverlay);

        // East face (X+)
        RenderUtils.addVertex(builder, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, packedLight, packedOverlay);
        RenderUtils.addVertex(builder, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, packedLight, packedOverlay);

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
