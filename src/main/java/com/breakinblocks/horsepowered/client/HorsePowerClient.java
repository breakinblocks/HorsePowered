package com.breakinblocks.horsepowered.client;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blockentity.ModBlockEntities;
import com.breakinblocks.horsepowered.client.renderer.ChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.DryingRackBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GeneratorBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GraniteAnvilBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.GrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.HandGrindstoneBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.ManualChopperBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.PressBlockEntityRenderer;
import com.breakinblocks.horsepowered.client.renderer.WorkSaddleSpecialRenderer;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

@EventBusSubscriber(modid = HorsePowerMod.MOD_ID, value = Dist.CLIENT)
public class HorsePowerClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.HAND_GRINDSTONE.get(), HandGrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRINDSTONE.get(), GrindstoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHOPPING_BLOCK.get(), ManualChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHOPPER.get(), ChopperBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESS.get(), PressBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GENERATOR.get(), GeneratorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRANITE_ANVIL.get(), GraniteAnvilBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(HorsePowerMod.id("work_saddle_entity"), WorkSaddleSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        // Seed oil uses custom pre-colored textures (no runtime tint needed)
        FluidModel.Unbaked seedOilModel = new FluidModel.Unbaked(
                new Material(HorsePowerMod.id("block/seed_oil_still")),
                new Material(HorsePowerMod.id("block/seed_oil_flow")),
                new Material(HorsePowerMod.id("block/seed_oil_still")),
                null // no tint — color is baked into the texture
        );
        event.register(seedOilModel,
                ModFluids.SEED_OIL_SOURCE.get(),
                ModFluids.SEED_OIL_FLOWING.get());
    }
}
