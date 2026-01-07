package com.breakinblocks.horsepowered;

import com.breakinblocks.horsepowered.blockentity.ModBlockEntities;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(HorsePowerMod.MOD_ID)
public class HorsePowerMod {

    public static final String MOD_ID = "horsepowered";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MOD_ID))
                    .icon(() -> new ItemStack(ModBlocks.HAND_GRINDSTONE.get()))
                    .displayItems((parameters, output) -> {
                        // Guide book (only if GuideME is loaded)
                        if (ModList.get().isLoaded("guideme")) {
                            output.accept(com.breakinblocks.horsepowered.compat.guideme.GuideMECompat.createGuideItem());
                        }
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

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public HorsePowerMod(IEventBus modEventBus, ModContainer container, Dist dist) {
        // Register blocks first, then items (block items depend on blocks), then block entities
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        HPRecipes.RECIPE_TYPES.register(modEventBus);
        HPRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        HPRecipes.RECIPE_BOOK_CATEGORIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register config
        container.registerConfig(ModConfig.Type.COMMON, HorsePowerConfig.SPEC);

        // Register common setup listener
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeContents);

        // Client-only setup - registration is handled by @EventBusSubscriber in HorsePowerClient
        if (dist.isClient()) {
            registerClientExtensions(container);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Horse Powered common setup");
    }

    private void buildCreativeContents(final BuildCreativeModeTabContentsEvent event) {
        // Items are added via the creative tab builder
    }

    /**
     * Client-only extension registration.
     * This method is only called on the client side to avoid loading client classes on server.
     */
    private static void registerClientExtensions(ModContainer container) {
        // Delegate to client helper class - this defers class loading of client-only classes
        com.breakinblocks.horsepowered.client.ClientExtensions.register(container);
    }
}
