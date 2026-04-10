package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;

/**
 * Renders the chopping blade for the horse-powered chopper.
 * The blade moves up and down like a guillotine based on the windup progress.
 */
public class BladeRenderer {

    // Blade dimensions (in block units, 1/16)
    private static final float BLADE_MIN_X = 3f / 16f;
    private static final float BLADE_MAX_X = 13f / 16f;
    private static final float BLADE_MIN_Y = 23f / 16f;
    private static final float BLADE_MAX_Y = 26f / 16f;
    private static final float BLADE_MIN_Z = 7.5f / 16f;
    private static final float BLADE_MAX_Z = 8.5f / 16f;

    private static final float BLADE_TRAVEL = 0.5f;

    /**
     * Renders the chopping blade for the chopper.
     *
     * @param poseStack The pose stack (already translated to block position)
     * @param collector The node collector for submitting geometry
     * @param visualWindup The windup value (-0.74 = down/chopped, 0 = up/raised)
     * @param packedLight The packed light value for proper lighting
     */
    public static void renderBlade(PoseStack poseStack, SubmitNodeCollector collector, float visualWindup, int packedLight) {
        poseStack.pushPose();

        float yOffset = visualWindup * BLADE_TRAVEL / 0.74f;
        poseStack.translate(0, yOffset, 0);

        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                .getBlockStateModelSet().getParticleMaterial(Blocks.IRON_BLOCK.defaultBlockState()).sprite();

        collector.submitCustomGeometry(
                poseStack,
                Sheets.cutoutBlockSheet(),
                (pose, consumer) -> RenderUtils.renderTexturedBox(consumer, pose, sprite,
                        BLADE_MIN_X, BLADE_MIN_Y, BLADE_MIN_Z,
                        BLADE_MAX_X, BLADE_MAX_Y, BLADE_MAX_Z,
                        packedLight)
        );

        poseStack.popPose();
    }
}
