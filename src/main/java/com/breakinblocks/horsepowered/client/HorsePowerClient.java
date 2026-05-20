package com.breakinblocks.horsepowered.client;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.client.renderer.AnimalTrapBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.ChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.DryingRackBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GeneratorBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GraniteAnvilBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.HandGrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.ManualChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.PressBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.WorkSaddleItemRenderer;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import com.breakinblocks.horsepowered.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = HorsePowerMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HorsePowerClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.HAND_GRINDSTONE_BE.get(), HandGrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.GRINDSTONE_BE.get(), GrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CHOPPING_BLOCK_BE.get(), ManualChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CHOPPER_BE.get(), ChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.PRESS_BE.get(), PressBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.GENERATOR_BE.get(), GeneratorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.DRYING_RACK_BE.get(), DryingRackBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.GRANITE_ANVIL_BE.get(), GraniteAnvilBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.ANIMAL_TRAP_BE.get(), AnimalTrapBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        // Color is baked into the texture — no tint needed
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return HorsePowerMod.id("block/seed_oil_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return HorsePowerMod.id("block/seed_oil_flow");
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return HorsePowerMod.id("block/seed_oil_still");
            }
        }, ModFluids.SEED_OIL_TYPE.get());

        Minecraft mc = Minecraft.getInstance();
        WorkSaddleItemRenderer workSaddleRenderer =
                new WorkSaddleItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return workSaddleRenderer;
            }
        }, ModItems.WORK_SADDLE.get());
    }
}
