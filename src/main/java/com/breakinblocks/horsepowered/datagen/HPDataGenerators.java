package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data generators for Horse Powered.
 * In 26.1, data generation is split into client and server events.
 */
@EventBusSubscriber(modid = HorsePowerMod.MOD_ID)
public class HPDataGenerators {

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        HorsePowerMod.LOGGER.info("Horse Powered server data generation starting...");
        event.createProvider(HPRecipeProvider::new);
    }

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        HorsePowerMod.LOGGER.info("Horse Powered client data generation starting...");
        // Client data providers (models, blockstates, lang, etc.) go here
    }
}
