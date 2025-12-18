package com.breakinblocks.horsepowered.items;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HorsePowerMod.MOD_ID);

    public static final DeferredItem<Item> FLOUR = ITEMS.register("flour",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DOUGH = ITEMS.register("dough",
            () -> new Item(new Item.Properties()));
}
