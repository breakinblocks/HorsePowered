package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;

public class PlungerRenderer {

    private static final float PLUNGER_MIN_X = 4f / 16f;
    private static final float PLUNGER_MAX_X = 12f / 16f;
    private static final float PLUNGER_MIN_Y = 20f / 16f;
    private static final float PLUNGER_MAX_Y = 24f / 16f;
    private static final float PLUNGER_MIN_Z = 4f / 16f;
    private static final float PLUNGER_MAX_Z = 12f / 16f;

    private static final float PLUNGER_TRAVEL = 0.5f;

    // visualProgress: 0.0 = raised, 1.0 = pressing down.
    public static void renderPlunger(PoseStack poseStack, SubmitNodeCollector collector, float visualProgress, int packedLight) {
        poseStack.pushPose();

        float yOffset = -visualProgress * PLUNGER_TRAVEL;
        poseStack.translate(0, yOffset, 0);

        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                .getBlockStateModelSet().getParticleMaterial(Blocks.STONE.defaultBlockState()).sprite();

        collector.submitCustomGeometry(
                poseStack,
                Sheets.cutoutBlockSheet(),
                (pose, consumer) -> RenderUtils.renderTexturedBox(consumer, pose, sprite,
                        PLUNGER_MIN_X, PLUNGER_MIN_Y, PLUNGER_MIN_Z,
                        PLUNGER_MAX_X, PLUNGER_MAX_Y, PLUNGER_MAX_Z,
                        packedLight)
        );

        poseStack.popPose();
    }
}
