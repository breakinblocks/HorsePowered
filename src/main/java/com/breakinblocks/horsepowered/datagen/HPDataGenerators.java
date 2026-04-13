package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = HorsePowerMod.MOD_ID)
public class HPDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        HorsePowerMod.LOGGER.info("Horse Powered data generation starting...");
        event.createProvider(HPRecipeProvider::new);
    }
}
