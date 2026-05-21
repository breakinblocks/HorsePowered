package com.breakinblocks.horsepowered.items;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.fluids.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HorsePowerMod.MOD_ID);

    public static final DeferredItem<Item> FLOUR = ITEMS.register("flour",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DOUGH = ITEMS.register("dough",
            () -> new Item(new Item.Properties()));

    // Seed oil bucket
    public static final DeferredItem<BucketItem> SEED_OIL_BUCKET = ITEMS.register("seed_oil_bucket",
            () -> new BucketItem(ModFluids.SEED_OIL_SOURCE.get(), new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    public static final DeferredItem<WorkSaddleItem> WORK_SADDLE = ITEMS.register("work_saddle",
            () -> new WorkSaddleItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<FlintAndTinderItem> FLINT_AND_TINDER = ITEMS.register("flint_and_tinder",
            () -> new FlintAndTinderItem(new Item.Properties().durability(8)));
}
