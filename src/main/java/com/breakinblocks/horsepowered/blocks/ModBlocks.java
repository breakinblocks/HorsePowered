package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.CreativeBatteryBlockEntity;
import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GeneratorBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GraniteAnvilBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blockentity.WoodenHopperBlockEntity;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MODID);

    public static final RegistryObject<Block> HAND_GRINDSTONE = registerBlock("hand_grindstone",
            () -> new BlockHandGrindstone(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> GRINDSTONE = registerBlock("grindstone",
            () -> new BlockGrindstone(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CHOPPING_BLOCK = registerBlock("chopping_block",
            () -> new BlockChoppingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)));

    public static final RegistryObject<Block> CHOPPER = registerBlock("chopper",
            () -> new BlockChopper(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final RegistryObject<Block> PRESS = registerBlock("press",
            () -> new BlockPress(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final RegistryObject<Block> GENERATOR = registerBlock("generator",
            () -> new BlockGenerator(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> DRYING_RACK = registerBlock("drying_rack",
            () -> new BlockDryingRack(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .noOcclusion()));

    public static final RegistryObject<Block> CREATIVE_BATTERY = registerBlock("creative_battery",
            () -> new BlockCreativeBattery(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> FILLER = BLOCKS.register("filler",
            () -> new BlockFiller(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final RegistryObject<Block> WOODEN_HOPPER = registerBlock("wooden_hopper",
            () -> new BlockWoodenHopper(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final RegistryObject<Block> GRANITE_ANVIL = registerBlock("granite_anvil",
            () -> new BlockGraniteAnvil(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_PINK)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<BlockEntityType<HandGrindstoneBlockEntity>> HAND_GRINDSTONE_BE =
            BLOCK_ENTITIES.register("hand_grindstone", () ->
                    BlockEntityType.Builder.of(HandGrindstoneBlockEntity::new, HAND_GRINDSTONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<GrindstoneBlockEntity>> GRINDSTONE_BE =
            BLOCK_ENTITIES.register("grindstone", () ->
                    BlockEntityType.Builder.of(GrindstoneBlockEntity::new, GRINDSTONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ManualChopperBlockEntity>> CHOPPING_BLOCK_BE =
            BLOCK_ENTITIES.register("chopping_block", () ->
                    BlockEntityType.Builder.of(ManualChopperBlockEntity::new, CHOPPING_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChopperBlockEntity>> CHOPPER_BE =
            BLOCK_ENTITIES.register("chopper", () ->
                    BlockEntityType.Builder.of(ChopperBlockEntity::new, CHOPPER.get()).build(null));

    public static final RegistryObject<BlockEntityType<PressBlockEntity>> PRESS_BE =
            BLOCK_ENTITIES.register("press", () ->
                    BlockEntityType.Builder.of(PressBlockEntity::new, PRESS.get()).build(null));

    public static final RegistryObject<BlockEntityType<GeneratorBlockEntity>> GENERATOR_BE =
            BLOCK_ENTITIES.register("generator", () ->
                    BlockEntityType.Builder.of(GeneratorBlockEntity::new, GENERATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BE =
            BLOCK_ENTITIES.register("drying_rack", () ->
                    BlockEntityType.Builder.of(DryingRackBlockEntity::new, DRYING_RACK.get()).build(null));

    public static final RegistryObject<BlockEntityType<CreativeBatteryBlockEntity>> CREATIVE_BATTERY_BE =
            BLOCK_ENTITIES.register("creative_battery", () ->
                    BlockEntityType.Builder.of(CreativeBatteryBlockEntity::new, CREATIVE_BATTERY.get()).build(null));

    public static final RegistryObject<BlockEntityType<FillerBlockEntity>> FILLER_BE =
            BLOCK_ENTITIES.register("filler", () ->
                    BlockEntityType.Builder.of(FillerBlockEntity::new, FILLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<WoodenHopperBlockEntity>> WOODEN_HOPPER_BE =
            BLOCK_ENTITIES.register("wooden_hopper", () ->
                    BlockEntityType.Builder.of(WoodenHopperBlockEntity::new, WOODEN_HOPPER.get()).build(null));

    public static final RegistryObject<BlockEntityType<GraniteAnvilBlockEntity>> GRANITE_ANVIL_BE =
            BLOCK_ENTITIES.register("granite_anvil", () ->
                    BlockEntityType.Builder.of(GraniteAnvilBlockEntity::new, GRANITE_ANVIL.get()).build(null));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registeredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, registeredBlock);
        return registeredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
