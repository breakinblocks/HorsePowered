package com.breakinblocks.horsepowered.client.renderer;

import com.breakinblocks.horsepowered.Configs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public final class RenderUtils {

    private RenderUtils() {
    }

    public static void renderItemCountBillboard(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                                int packedLight, int count, double x, double y, double z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Component text = Component.literal(Integer.toString(count));
        float textX = -font.width(text) / 2.0F;

        Matrix4f matrix = poseStack.last().pose();

        // Matches how vanilla draws name tags: a translucent see-through pass carrying the
        // background, then an opaque pass in front, with the background alpha taken from the
        // Text Background accessibility option instead of a fixed value.
        int background = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        font.drawInBatch(text, textX, 0, 553648127, false, matrix, bufferSource,
                Font.DisplayMode.SEE_THROUGH, background, packedLight);
        font.drawInBatch(text, textX, 0, -1, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    public static void renderFlatItem(PoseStack poseStack, MultiBufferSource bufferSource,
                                       ItemRenderer itemRenderer, Font font, ItemStack stack,
                                       double x, double y, double z, float scale, float yRotation,
                                       int packedLight, int packedOverlay, Level level, double countY) {
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        if (yRotation != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, level, 0);

        poseStack.popPose();

        if (stack.getCount() > 1 && Configs.renderItemAmount.get()) {
            renderItemCountBillboard(poseStack, bufferSource, font, packedLight,
                    stack.getCount(), x, countY, z);
        }
    }

    public static void renderFlatItemFading(PoseStack poseStack, MultiBufferSource bufferSource,
                                            ItemRenderer itemRenderer, ItemStack stack,
                                            double x, double y, double z, float scale, float yRotation,
                                            int packedLight, int packedOverlay, Level level, float alpha) {
        if (stack.isEmpty() || alpha <= 0F) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        if (yRotation != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        MultiBufferSource alphaSource = new TransparentMultiBufferSource(bufferSource, alpha);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, alphaSource, level, 0);

        poseStack.popPose();
    }

    public static void renderStandingItem(PoseStack poseStack, MultiBufferSource bufferSource,
                                           ItemRenderer itemRenderer, Font font, ItemStack stack,
                                           double x, double y, double z, float scale, float yRotation,
                                           int packedLight, int packedOverlay, Level level, double countY) {
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        if (yRotation != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, level, 0);

        poseStack.popPose();

        if (stack.getCount() > 1 && Configs.renderItemAmount.get()) {
            renderItemCountBillboard(poseStack, bufferSource, font, packedLight,
                    stack.getCount(), x, countY, z);
        }
    }

    public static void addVertex(VertexConsumer builder, Matrix4f pose,
                                 float x, float y, float z, float u, float v,
                                 float nx, float ny, float nz, int packedLight) {
        builder.vertex(pose, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .uv2(packedLight)
                .normal(nx, ny, nz)
                .endVertex();
    }

    public static void renderTexturedBox(PoseStack poseStack, VertexConsumer builder, TextureAtlasSprite sprite,
                                         int packedLight, float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ) {
        Matrix4f pose = poseStack.last().pose();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 1, 0, packedLight);
        addVertex(builder, pose, minX, maxY, maxZ, u0, v1, 0, 1, 0, packedLight);
        addVertex(builder, pose, maxX, maxY, maxZ, u1, v1, 0, 1, 0, packedLight);
        addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 1, 0, packedLight);

        addVertex(builder, pose, minX, minY, maxZ, u0, v0, 0, -1, 0, packedLight);
        addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, -1, 0, packedLight);
        addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, -1, 0, packedLight);
        addVertex(builder, pose, maxX, minY, maxZ, u1, v0, 0, -1, 0, packedLight);

        addVertex(builder, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, packedLight);
        addVertex(builder, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, packedLight);
        addVertex(builder, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, packedLight);
        addVertex(builder, pose, minX, minY, minZ, u0, v1, 0, 0, -1, packedLight);

        addVertex(builder, pose, maxX, maxY, maxZ, u0, v0, 0, 0, 1, packedLight);
        addVertex(builder, pose, minX, maxY, maxZ, u1, v0, 0, 0, 1, packedLight);
        addVertex(builder, pose, minX, minY, maxZ, u1, v1, 0, 0, 1, packedLight);
        addVertex(builder, pose, maxX, minY, maxZ, u0, v1, 0, 0, 1, packedLight);

        addVertex(builder, pose, minX, maxY, maxZ, u0, v0, -1, 0, 0, packedLight);
        addVertex(builder, pose, minX, maxY, minZ, u1, v0, -1, 0, 0, packedLight);
        addVertex(builder, pose, minX, minY, minZ, u1, v1, -1, 0, 0, packedLight);
        addVertex(builder, pose, minX, minY, maxZ, u0, v1, -1, 0, 0, packedLight);

        addVertex(builder, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, packedLight);
        addVertex(builder, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, packedLight);
        addVertex(builder, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, packedLight);
        addVertex(builder, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, packedLight);
    }
}
