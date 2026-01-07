package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HorsePowerMod.MOD_ID);

    // Blocks - block items are registered in ModItems
    // In NeoForge 21.11+, you must use .setId() on BlockBehaviour.Properties
    public static final DeferredBlock<Block> HAND_GRINDSTONE = BLOCKS.register("hand_grindstone",
            registryName -> new BlockHandGrindstone(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GRINDSTONE = BLOCKS.register("grindstone",
            registryName -> new BlockGrindstone(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CHOPPING_BLOCK = BLOCKS.register("chopping_block",
            registryName -> new BlockChoppingBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)));

    public static final DeferredBlock<Block> CHOPPER = BLOCKS.register("chopper",
            registryName -> new BlockChopper(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final DeferredBlock<Block> PRESS = BLOCKS.register("press",
            registryName -> new BlockPress(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final DeferredBlock<Block> FILLER = BLOCKS.register("filler",
            registryName -> new BlockFiller(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)
                    .noOcclusion()));
}
