package com.breakinblocks.horsepowered;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityBase;
import com.breakinblocks.horsepowered.blockentity.ModBlockEntities;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.client.ClientExtensions;
import com.breakinblocks.horsepowered.compat.guideme.GuideMECompat;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
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
                        if (ModList.get().isLoaded("guideme")) {
                            output.accept(GuideMECompat.createGuideItem());
                        }
                        output.accept(ModItems.FLOUR.get());
                        output.accept(ModItems.DOUGH.get());
                        output.accept(ModItems.SEED_OIL_BUCKET.get());
                        output.accept(ModItems.WORK_SADDLE.get());
                        output.accept(ModBlocks.HAND_GRINDSTONE.get());
                        output.accept(ModBlocks.GRINDSTONE.get());
                        output.accept(ModBlocks.CHOPPING_BLOCK.get());
                        output.accept(ModBlocks.CHOPPER.get());
                        output.accept(ModBlocks.PRESS.get());
                        output.accept(ModBlocks.GENERATOR.get());
                        output.accept(ModBlocks.DRYING_RACK.get());
                        output.accept(ModBlocks.WOODEN_HOPPER.get());
                        output.accept(ModBlocks.CREATIVE_BATTERY.get());
                    })
                    .build()
    );

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public HorsePowerMod(IEventBus modEventBus, ModContainer container, Dist dist) {
        NeoForgeMod.enableMilkFluid();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        HPRecipes.RECIPE_TYPES.register(modEventBus);
        HPRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        HPRecipes.RECIPE_BOOK_CATEGORIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        container.registerConfig(ModConfig.Type.COMMON, HorsePowerConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, HorsePowerConfig.CLIENT_SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeContents);
        modEventBus.addListener(HorsePowerMod::registerCapabilities);

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
            var air = Blocks.AIR.defaultBlockState();

            FluidInteractionRegistry.addInteraction(oilType,
                    new FluidInteractionRegistry.InteractionInformation(
                            lavaType,
                            fluidState -> fire));

            FluidInteractionRegistry.addInteraction(oilType,
                    new FluidInteractionRegistry.InteractionInformation(
                            (level, currentPos, relativePos, currentState) ->
                                    level.getBlockState(relativePos).is(Blocks.FIRE),
                            fluidState -> fire));

            FluidInteractionRegistry.addInteraction(lavaType,
                    new FluidInteractionRegistry.InteractionInformation(
                            oilType,
                            fluidState -> fire));

            LOGGER.info("Registered seed oil fluid interactions");
        });
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.CHOPPER.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.GRINDSTONE.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.PRESS.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.HAND_GRINDSTONE.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.CHOPPING_BLOCK.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.DRYING_RACK.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.GENERATOR.get(),
                (be, side) -> be.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.CREATIVE_BATTERY.get(),
                (be, side) -> be.getEnergyHandler());

        event.registerBlockEntity(Capabilities.Fluid.BLOCK,
                ModBlockEntities.PRESS.get(),
                (be, side) -> be.getFluidHandler());

        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.FILLER.get(),
                (be, side) -> {
                    HPBlockEntityBase mainBe = be.getFilledTileEntity();
                    if (mainBe != null) {
                        return new WorldlyContainerWrapper(mainBe, side);
                    }
                    return null;
                });
        event.registerBlockEntity(Capabilities.Fluid.BLOCK,
                ModBlockEntities.FILLER.get(),
                (be, side) -> {
                    HPBlockEntityBase mainBe = be.getFilledTileEntity();
                    if (mainBe instanceof PressBlockEntity press) {
                        return press.getFluidHandler();
                    }
                    return null;
                });

        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.WOODEN_HOPPER.get(),
                (be, side) -> VanillaContainerWrapper.of(be));
    }

    private void buildCreativeContents(final BuildCreativeModeTabContentsEvent event) {
    }

    private static void registerClientExtensions(ModContainer container) {
        ClientExtensions.register(container);
    }
}
