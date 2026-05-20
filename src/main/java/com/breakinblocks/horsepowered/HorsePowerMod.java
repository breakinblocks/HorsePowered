package com.breakinblocks.horsepowered;

import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.client.ClientExtensions;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(HorsePowerMod.MOD_ID)
public class HorsePowerMod {

    public static final String MOD_ID = "horsepowered";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MOD_ID))
                    .icon(() -> new ItemStack(ModBlocks.HAND_GRINDSTONE.get()))
                    .displayItems((parameters, output) -> {
                        // Items
                        output.accept(ModItems.FLOUR.get());
                        output.accept(ModItems.DOUGH.get());
                        output.accept(ModItems.SEED_OIL_BUCKET.get());
                        output.accept(ModItems.WORK_SADDLE.get());
                        output.accept(ModItems.FLINT_AND_TINDER.get());
                        // Blocks
                        output.accept(ModBlocks.HAND_GRINDSTONE.get());
                        output.accept(ModBlocks.GRINDSTONE.get());
                        output.accept(ModBlocks.CHOPPING_BLOCK.get());
                        output.accept(ModBlocks.GRANITE_ANVIL.get());
                        output.accept(ModBlocks.CHOPPER.get());
                        output.accept(ModBlocks.PRESS.get());
                        output.accept(ModBlocks.GENERATOR.get());
                        output.accept(ModBlocks.DRYING_RACK.get());
                        output.accept(ModBlocks.WOODEN_HOPPER.get());
                        output.accept(ModBlocks.CREATIVE_BATTERY.get());
                    })
                    .build()
    );

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public HorsePowerMod(IEventBus modEventBus, ModContainer container, Dist dist) {
        // Register items and blocks
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        HPRecipes.RECIPE_TYPES.register(modEventBus);
        HPRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register configs
        container.registerConfig(ModConfig.Type.COMMON, HorsePowerConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, HorsePowerConfig.CLIENT_SPEC);

        // Register common setup listener
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeContents);
        modEventBus.addListener(HorsePowerMod::registerCapabilities);

        // Client-only setup - registration is handled by @EventBusSubscriber in HorsePowerClient
        if (dist.isClient()) {
            registerClientExtensions(container);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Horse Powered common setup");
        event.enqueueWork(() -> {
            var lavaType = NeoForgeMod.LAVA_TYPE.value();
            var oilType = ModFluids.SEED_OIL_TYPE.get();
            var fire = Blocks.FIRE.defaultBlockState();

            // Seed oil + adjacent lava → fire
            FluidInteractionRegistry.addInteraction(oilType,
                    new FluidInteractionRegistry.InteractionInformation(
                            lavaType, fluidState -> fire));

            // Seed oil + adjacent fire block → fire
            FluidInteractionRegistry.addInteraction(oilType,
                    new FluidInteractionRegistry.InteractionInformation(
                            (level, currentPos, relativePos, currentState) ->
                                    level.getBlockState(relativePos).is(Blocks.FIRE),
                            fluidState -> fire));

            // Lava + adjacent seed oil → fire
            FluidInteractionRegistry.addInteraction(lavaType,
                    new FluidInteractionRegistry.InteractionInformation(
                            oilType, fluidState -> fire));

            LOGGER.info("Registered seed oil fluid interactions");
        });
    }

    private void buildCreativeContents(final BuildCreativeModeTabContentsEvent event) {
        // Items are added via the creative tab builder
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Press exposes a directional fluid handler: insertions go to the input tank,
        // extractions drain the output tank. Prevents pipes from contaminating input
        // with arbitrary fluids or stealing reagents mid-process.
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlocks.PRESS_BE.get(),
                (be, side) -> be.getFluidHandler());

        // Filler blocks delegate to the press when they sit above one — pipes can connect
        // to either half of the multi-block.
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlocks.FILLER_BE.get(),
                (be, side) -> {
                    var mainBe = be.getFilledTileEntity();
                    if (mainBe instanceof PressBlockEntity press) {
                        return press.getFluidHandler();
                    }
                    return null;
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlocks.GENERATOR_BE.get(),
                (be, side) -> be.getEnergyHandler());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlocks.CREATIVE_BATTERY_BE.get(),
                (be, side) -> be.getEnergyHandler());

        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> {
                    DryingRackBlockEntity main = BlockDryingRack.getMainBlockEntity(level, state, pos);
                    return main == null ? null : main.getItemHandler();
                },
                ModBlocks.DRYING_RACK.get());

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlocks.WOODEN_HOPPER_BE.get(),
                (be, side) -> new InvWrapper(be));
    }

    /**
     * Client-only extension registration.
     * This method is only called on the client side to avoid loading client classes on server.
     */
    private static void registerClientExtensions(ModContainer container) {
        // Delegate to client helper class - this defers class loading of client-only classes
        ClientExtensions.register(container);
    }
}
