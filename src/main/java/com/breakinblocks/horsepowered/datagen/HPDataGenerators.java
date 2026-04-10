package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Data generators for Horse Powered.
 * Uses 1.21.1's unified GatherDataEvent (not split like 26.1+).
 */
@EventBusSubscriber(modid = HorsePowerMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class HPDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        HorsePowerMod.LOGGER.info("Horse Powered data generation starting...");
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new HPRecipeProvider(packOutput, lookup));
    }
}
