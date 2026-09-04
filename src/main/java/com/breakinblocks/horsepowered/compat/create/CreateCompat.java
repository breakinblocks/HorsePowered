package com.breakinblocks.horsepowered.compat.create;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.compat.create.client.CreateCompatClient;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.lib.Reference;
import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.MinecraftForge;

public final class CreateCompat {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MODID);

    public static final RegistryObject<HorseEngineBlock> HORSE_ENGINE = BLOCKS.register("horse_engine",
            () -> new HorseEngineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> HORSE_ENGINE_ITEM = ITEMS.register("horse_engine",
            () -> new BlockItem(HORSE_ENGINE.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<HorseEngineBlockEntity>> HORSE_ENGINE_BE =
            BLOCK_ENTITIES.register("horse_engine", () ->
                    BlockEntityType.Builder.of(HorseEngineBlockEntity::new, HORSE_ENGINE.get()).build(null));

    private CreateCompat() {
    }

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(CreateCompat::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(CreateCompat::addReloadListener);
        if (FMLEnvironment.dist.isClient()) {
            CreateCompatClient.init(modBus);
        }
    }

    public static void addToCreativeTab(net.minecraft.world.item.CreativeModeTab.Output output) {
        output.accept(HORSE_ENGINE_ITEM.get());
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        Block block = HORSE_ENGINE.get();
        BlockStressValues.RPM.register(block,
                new BlockStressValues.GeneratedRpm(Configs.horseEngineRpm.get(), false));
        BlockStressValues.CAPACITIES.register(block,
                () -> HorseEngineBlockEntity.capacityPerRpm(Configs.horseEngineDefaultJumpStrength.get()));
    }

    private static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new JumpStrengthOverrides());
    }
}
