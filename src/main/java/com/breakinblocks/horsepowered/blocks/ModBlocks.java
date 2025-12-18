package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.blockentity.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MODID);

    // Blocks
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

    public static final RegistryObject<Block> FILLER = BLOCKS.register("filler",
            () -> new BlockFiller(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)
                    .noOcclusion()));

    // Block Entities
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

    public static final RegistryObject<BlockEntityType<FillerBlockEntity>> FILLER_BE =
            BLOCK_ENTITIES.register("filler", () ->
                    BlockEntityType.Builder.of(FillerBlockEntity::new, FILLER.get()).build(null));

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
