package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data generators for Horse Powered.
 * GatherDataEvent implements IModBusEvent so it automatically routes to the mod event bus.
 */
@EventBusSubscriber(modid = HorsePowerMod.MOD_ID)
public class HPDataGenerators {

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        HorsePowerMod.LOGGER.info("Horse Powered server data generation starting...");
        event.createProvider(HPRecipeProvider::new);
    }
}
