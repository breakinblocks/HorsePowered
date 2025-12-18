package com.breakinblocks.horsepowered.client;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.client.renderer.ChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.PressBlockEntityRenderer;
import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.GRINDSTONE_BE.get(), GrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CHOPPER_BE.get(), ChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.PRESS_BE.get(), PressBlockEntityRenderer::new);
    }
}
