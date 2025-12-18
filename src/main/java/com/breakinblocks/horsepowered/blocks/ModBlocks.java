package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blockentity.*;
import com.breakinblocks.horsepowered.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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

    public static final DeferredBlock<Block> FILLER = BLOCKS.register("filler",
            () -> new BlockFiller(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)
                    .noOcclusion()));

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
