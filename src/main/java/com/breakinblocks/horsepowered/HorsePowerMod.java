package com.breakinblocks.horsepowered;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Reference.MODID)
public class HorsePowerMod {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MODID);

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Reference.MODID))
                    .icon(() -> new ItemStack(ModBlocks.HAND_GRINDSTONE.get()))
                    .displayItems((parameters, output) -> {
                        // Items
                        output.accept(ModItems.FLOUR.get());
                        output.accept(ModItems.DOUGH.get());
                        // Blocks
                        output.accept(ModBlocks.HAND_GRINDSTONE.get());
                        output.accept(ModBlocks.GRINDSTONE.get());
                        output.accept(ModBlocks.CHOPPING_BLOCK.get());
                        output.accept(ModBlocks.CHOPPER.get());
                        output.accept(ModBlocks.PRESS.get());
                    })
                    .build()
    );

    public HorsePowerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register items and blocks
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        HPRecipes.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.SPEC);

        // Register common setup listener
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Horse Powered common setup");
    }
}
