package com.breakinblocks.horsepowered.compat.create;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.compat.create.client.CreateCompatClient;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.mojang.serialization.Codec;
import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class CreateCompat {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HorsePowerMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HorsePowerMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HorsePowerMod.MOD_ID);

    public static final DataMapType<EntityType<?>, Double> JUMP_STRENGTH = DataMapType
            .builder(HorsePowerMod.id("horse_engine_jump_strength"), Registries.ENTITY_TYPE, Codec.DOUBLE)
            .build();

    public static final DeferredBlock<HorseEngineBlock> HORSE_ENGINE = BLOCKS.register("horse_engine",
            () -> new HorseEngineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredItem<BlockItem> HORSE_ENGINE_ITEM = ITEMS.register("horse_engine",
            () -> new BlockItem(HORSE_ENGINE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HorseEngineBlockEntity>> HORSE_ENGINE_BE =
            BLOCK_ENTITIES.register("horse_engine", () ->
                    BlockEntityType.Builder.of(HorseEngineBlockEntity::new, HORSE_ENGINE.get()).build(null));

    private CreateCompat() {
    }

    public static void init(IEventBus modBus, Dist dist) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(CreateCompat::registerDataMaps);
        modBus.addListener(CreateCompat::commonSetup);
        modBus.addListener(CreateCompat::buildCreativeTab);
        if (dist.isClient()) {
            CreateCompatClient.init(modBus);
        }
    }

    private static void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(JUMP_STRENGTH);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        Block block = HORSE_ENGINE.get();
        BlockStressValues.RPM.register(block,
                new BlockStressValues.GeneratedRpm(HorsePowerConfig.horseEngineRpm.get(), false));
        BlockStressValues.CAPACITIES.register(block,
                () -> HorseEngineBlockEntity.capacityPerRpm(HorsePowerConfig.horseEngineDefaultJumpStrength.get()));
    }

    private static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == HorsePowerMod.CREATIVE_TAB.getKey()) {
            event.accept(HORSE_ENGINE_ITEM.get());
        }
    }
}
