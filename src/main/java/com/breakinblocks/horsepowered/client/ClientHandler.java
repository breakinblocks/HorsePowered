package com.breakinblocks.horsepowered.client;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.client.renderer.ChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.DryingRackBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GeneratorBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.HandGrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.ManualChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.PressBlockEntityRenderer;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.HAND_GRINDSTONE_BE.get(), HandGrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.GRINDSTONE_BE.get(), GrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CHOPPING_BLOCK_BE.get(), ManualChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CHOPPER_BE.get(), ChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.PRESS_BE.get(), PressBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.GENERATOR_BE.get(), GeneratorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.DRYING_RACK_BE.get(), DryingRackBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                ItemProperties.register(ModItems.WORK_SADDLE.get(),
                        new ResourceLocation(Reference.MODID, "carrying"),
                        (stack, level, entity, seed) -> {
                            CompoundTag tag = stack.getTag();
                            return (tag != null && tag.contains("CarriedEntity")) ? 1.0F : 0.0F;
                        }));
    }
}
