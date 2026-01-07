package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Block entity registration - separate from ModBlocks to ensure proper loading order.
 * Block entities depend on blocks being registered first.
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HorsePowerMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HandGrindstoneBlockEntity>> HAND_GRINDSTONE =
            registerBlockEntity("hand_grindstone", HandGrindstoneBlockEntity::new, () -> ModBlocks.HAND_GRINDSTONE.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrindstoneBlockEntity>> GRINDSTONE =
            registerBlockEntity("grindstone", GrindstoneBlockEntity::new, () -> ModBlocks.GRINDSTONE.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManualChopperBlockEntity>> CHOPPING_BLOCK =
            registerBlockEntity("chopping_block", ManualChopperBlockEntity::new, () -> ModBlocks.CHOPPING_BLOCK.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChopperBlockEntity>> CHOPPER =
            registerBlockEntity("chopper", ChopperBlockEntity::new, () -> ModBlocks.CHOPPER.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressBlockEntity>> PRESS =
            registerBlockEntity("press", PressBlockEntity::new, () -> ModBlocks.PRESS.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FillerBlockEntity>> FILLER =
            registerBlockEntity("filler", FillerBlockEntity::new, () -> ModBlocks.FILLER.get());

    /**
     * Helper method to register a block entity type with a single valid block.
     * The block supplier is evaluated lazily during registration, after blocks are registered.
     */
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Supplier<Block> blockSupplier) {
        return BLOCK_ENTITIES.register(name, () -> {
            Block block = blockSupplier.get();
            return new BlockEntityType<>(factory, java.util.Set.of(block));
        });
    }
}
