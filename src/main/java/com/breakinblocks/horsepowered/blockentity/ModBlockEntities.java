package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR =
            registerBlockEntity("generator", GeneratorBlockEntity::new, () -> ModBlocks.GENERATOR.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeBatteryBlockEntity>> CREATIVE_BATTERY =
            registerBlockEntity("creative_battery", CreativeBatteryBlockEntity::new, () -> ModBlocks.CREATIVE_BATTERY.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK =
            registerBlockEntity("drying_rack", DryingRackBlockEntity::new, () -> ModBlocks.DRYING_RACK.get());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenHopperBlockEntity>> WOODEN_HOPPER =
            registerBlockEntity("wooden_hopper", WoodenHopperBlockEntity::new, () -> ModBlocks.WOODEN_HOPPER.get());

    /**
     * Helper method to register a block entity type with a single valid block.
     * The block supplier is evaluated lazily during registration, after blocks are registered.
     */
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Supplier<Block> blockSupplier) {
        return BLOCK_ENTITIES.register(name, () -> {
            Block block = blockSupplier.get();
            return new BlockEntityType<>(factory, Set.of(block));
        });
    }
}
