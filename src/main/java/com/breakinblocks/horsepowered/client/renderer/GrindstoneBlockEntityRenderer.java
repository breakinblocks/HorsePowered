package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class GrindstoneBlockEntityRenderer implements BlockEntityRenderer<GrindstoneBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public GrindstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(GrindstoneBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // Render working area highlight if active
        WorkingAreaRenderer.renderIfActive(blockEntity, poseStack, bufferSource);

        // Render lead to attached worker
        LeadRenderer.renderLead(blockEntity, partialTick, poseStack, bufferSource);

        ItemStack input = blockEntity.getItem(0);
        ItemStack output = blockEntity.getItem(1);
        ItemStack secondary = blockEntity.getItem(2);

        // Render input item on top
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.1D, 0.5D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            // Render count if more than 1
            if (input.getCount() > 1 && Configs.renderItemAmount.get()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0.0D, 0.0D, -0.4D);
                renderItemCount(poseStack, bufferSource, packedLight, input.getCount());
            }

            poseStack.popPose();
        }

        // Render output item
        if (!output.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.3D, 0.25D);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(output, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            if (output.getCount() > 1 && Configs.renderItemAmount.get()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0.0D, 0.0D, -0.3D);
                renderItemCount(poseStack, bufferSource, packedLight, output.getCount());
            }

            poseStack.popPose();
        }

        // Render secondary output
        if (!secondary.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.3D, 0.75D);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(secondary, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            if (secondary.getCount() > 1 && Configs.renderItemAmount.get()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0.0D, 0.0D, -0.3D);
                renderItemCount(poseStack, bufferSource, packedLight, secondary.getCount());
            }

            poseStack.popPose();
        }
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
    public boolean shouldRenderOffScreen(GrindstoneBlockEntity blockEntity) {
        // Render even when grindstone is off-screen so highlights show properly
        return blockEntity.shouldShowHighlight();
    }

    @Override
    public int getViewDistance() {
        return 64; // Render from further away when highlighting
    }
}
