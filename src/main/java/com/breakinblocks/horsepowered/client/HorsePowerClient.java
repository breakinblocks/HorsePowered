package com.breakinblocks.horsepowered.client;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.client.renderer.ChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.PressBlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = HorsePowerMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HorsePowerClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.GRINDSTONE_BE.get(), GrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CHOPPER_BE.get(), ChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.PRESS_BE.get(), PressBlockEntityRenderer::new);
    }
}
