package com.breakinblocks.horsepowered.compat.create.client;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.compat.create.CreateCompat;
import com.breakinblocks.horsepowered.compat.create.HorseEngineBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class CreateCompatClient {

    public static PartialModel COG;

    private CreateCompatClient() {
    }

    public static void init(IEventBus modBus) {
        COG = PartialModel.of(HorsePowerMod.id("block/horse_engine/cog"));
        modBus.addListener(CreateCompatClient::registerRenderers);
        modBus.addListener(CreateCompatClient::clientSetup);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CreateCompat.HORSE_ENGINE_BE.get(), HorseEngineRenderer::new);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        SimpleBlockEntityVisualizer.builder(CreateCompat.HORSE_ENGINE_BE.get())
                .factory(SingleAxisRotatingVisual.<HorseEngineBlockEntity>of(COG))
                .skipVanillaRender(be -> false)
                .apply();
    }
}
