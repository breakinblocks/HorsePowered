package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HorsePowerMod.MOD_ID);

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

    public static final DeferredBlock<Block> GENERATOR = BLOCKS.register("generator",
            registryName -> new BlockGenerator(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.0F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CREATIVE_BATTERY = BLOCKS.register("creative_battery",
            registryName -> new BlockCreativeBattery(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<Block> DRYING_RACK = BLOCKS.register("drying_rack",
            registryName -> new BlockDryingRack(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .noOcclusion()));

    public static final DeferredBlock<Block> CHOPPING_BLOCK = BLOCKS.register("chopping_block",
            registryName -> new BlockChoppingBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)));

    public static final DeferredBlock<Block> GRANITE_ANVIL = BLOCKS.register("granite_anvil",
            registryName -> new BlockGraniteAnvil(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.TERRACOTTA_PINK)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

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

    // Fluid blocks — seed oil is flammable: high flammability (300) + instant spread (300)
    public static final DeferredBlock<LiquidBlock> SEED_OIL_BLOCK = BLOCKS.register("seed_oil",
            registryName -> new FlammableLiquidBlock(ModFluids.SEED_OIL_SOURCE.get(), BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .replaceable()
                    .noCollision()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid(),
                    300, 300));

    public static final DeferredBlock<Block> FILLER = BLOCKS.register("filler",
            registryName -> new BlockFiller(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(5.0F)));

    public static final DeferredBlock<Block> WOODEN_HOPPER = BLOCKS.register("wooden_hopper",
            registryName -> new BlockWoodenHopper(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));
}
