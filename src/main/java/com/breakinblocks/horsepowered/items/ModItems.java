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

    // Regular items - In NeoForge 21.11+, you must use .setId() on Item.Properties
    public static final DeferredItem<Item> FLOUR = ITEMS.register("flour",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredItem<Item> DOUGH = ITEMS.register("dough",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))));

    // Fluid buckets
    public static final DeferredItem<BucketItem> SEED_OIL_BUCKET = ITEMS.register("seed_oil_bucket",
            registryName -> new BucketItem(ModFluids.SEED_OIL_SOURCE.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    // Block items - register with lazy block lookup
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
}
