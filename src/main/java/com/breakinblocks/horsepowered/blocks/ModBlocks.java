package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.CreativeBatteryBlockEntity;
import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GeneratorBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import com.breakinblocks.horsepowered.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HorsePowerMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HorsePowerMod.MOD_ID);

    // Blocks
    public static final DeferredBlock<Block> HAND_GRINDSTONE = registerBlock("hand_grindstone",
            () -> new BlockHandGrindstone(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GRINDSTONE = registerBlock("grindstone",
            () -> new BlockGrindstone(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CHOPPING_BLOCK = registerBlock("chopping_block",
            () -> new BlockChoppingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)));

    public static final DeferredBlock<Block> CHOPPER = registerBlock("chopper",
            () -> new BlockChopper(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final DeferredBlock<Block> PRESS = registerBlock("press",
            () -> new BlockPress(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final DeferredBlock<Block> GENERATOR = registerBlock("generator",
            () -> new BlockGenerator(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.0F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> DRYING_RACK = registerBlock("drying_rack",
            () -> new BlockDryingRack(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .noOcclusion()));

    public static final DeferredBlock<Block> CREATIVE_BATTERY = registerBlock("creative_battery",
            () -> new BlockCreativeBattery(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<Block> FILLER = BLOCKS.register("filler",
            () -> new BlockFiller(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    // Seed oil liquid block — highly flammable so fire chain-spreads through it
    public static final DeferredBlock<LiquidBlock> SEED_OIL_BLOCK = BLOCKS.register("seed_oil",
            () -> new FlammableLiquidBlock(ModFluids.SEED_OIL_SOURCE.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid(),
                    300, 300));

    // Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HandGrindstoneBlockEntity>> HAND_GRINDSTONE_BE =
            BLOCK_ENTITIES.register("hand_grindstone", () ->
                    BlockEntityType.Builder.of(HandGrindstoneBlockEntity::new, HAND_GRINDSTONE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrindstoneBlockEntity>> GRINDSTONE_BE =
            BLOCK_ENTITIES.register("grindstone", () ->
                    BlockEntityType.Builder.of(GrindstoneBlockEntity::new, GRINDSTONE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManualChopperBlockEntity>> CHOPPING_BLOCK_BE =
            BLOCK_ENTITIES.register("chopping_block", () ->
                    BlockEntityType.Builder.of(ManualChopperBlockEntity::new, CHOPPING_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChopperBlockEntity>> CHOPPER_BE =
            BLOCK_ENTITIES.register("chopper", () ->
                    BlockEntityType.Builder.of(ChopperBlockEntity::new, CHOPPER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressBlockEntity>> PRESS_BE =
            BLOCK_ENTITIES.register("press", () ->
                    BlockEntityType.Builder.of(PressBlockEntity::new, PRESS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR_BE =
            BLOCK_ENTITIES.register("generator", () ->
                    BlockEntityType.Builder.of(GeneratorBlockEntity::new, GENERATOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BE =
            BLOCK_ENTITIES.register("drying_rack", () ->
                    BlockEntityType.Builder.of(DryingRackBlockEntity::new, DRYING_RACK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeBatteryBlockEntity>> CREATIVE_BATTERY_BE =
            BLOCK_ENTITIES.register("creative_battery", () ->
                    BlockEntityType.Builder.of(CreativeBatteryBlockEntity::new, CREATIVE_BATTERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FillerBlockEntity>> FILLER_BE =
            BLOCK_ENTITIES.register("filler", () ->
                    BlockEntityType.Builder.of(FillerBlockEntity::new, FILLER.get()).build(null));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registeredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, registeredBlock);
        return registeredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
