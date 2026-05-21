package com.breakinblocks.horsepowered.items;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HorsePowerMod.MOD_ID);

    public static final DeferredItem<Item> FLOUR = ITEMS.register("flour",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredItem<Item> DOUGH = ITEMS.register("dough",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredItem<BucketItem> SEED_OIL_BUCKET = ITEMS.register("seed_oil_bucket",
            registryName -> new BucketItem(ModFluids.SEED_OIL_SOURCE.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    public static final DeferredItem<WorkSaddleItem> WORK_SADDLE = ITEMS.register("work_saddle",
            registryName -> new WorkSaddleItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)));

    public static final DeferredItem<FlintAndTinderItem> FLINT_AND_TINDER = ITEMS.register("flint_and_tinder",
            registryName -> new FlintAndTinderItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .durability(8)));

    public static final DeferredItem<BlockItem> HAND_GRINDSTONE_ITEM = ITEMS.register("hand_grindstone",
            registryName -> new BlockItem(ModBlocks.HAND_GRINDSTONE.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> GRINDSTONE_ITEM = ITEMS.register("grindstone",
            registryName -> new BlockItem(ModBlocks.GRINDSTONE.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHOPPING_BLOCK_ITEM = ITEMS.register("chopping_block",
            registryName -> new BlockItem(ModBlocks.CHOPPING_BLOCK.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHOPPER_ITEM = ITEMS.register("chopper",
            registryName -> new BlockItem(ModBlocks.CHOPPER.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> PRESS_ITEM = ITEMS.register("press",
            registryName -> new BlockItem(ModBlocks.PRESS.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> GENERATOR_ITEM = ITEMS.register("generator",
            registryName -> new BlockItem(ModBlocks.GENERATOR.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CREATIVE_BATTERY_ITEM = ITEMS.register("creative_battery",
            registryName -> new BlockItem(ModBlocks.CREATIVE_BATTERY.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> DRYING_RACK_ITEM = ITEMS.register("drying_rack",
            registryName -> new BlockItem(ModBlocks.DRYING_RACK.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> WOODEN_HOPPER_ITEM = ITEMS.register("wooden_hopper",
            registryName -> new BlockItem(ModBlocks.WOODEN_HOPPER.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> GRANITE_ANVIL_ITEM = ITEMS.register("granite_anvil",
            registryName -> new BlockItem(ModBlocks.GRANITE_ANVIL.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> ANIMAL_TRAP_ITEM = ITEMS.register("animal_trap",
            registryName -> new BlockItem(ModBlocks.ANIMAL_TRAP.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .useBlockDescriptionPrefix()));
}
