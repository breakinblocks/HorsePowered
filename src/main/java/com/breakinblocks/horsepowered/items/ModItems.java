package com.breakinblocks.horsepowered.items;

import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MODID);

    // Basic items
    public static final RegistryObject<Item> FLOUR = ITEMS.register("flour", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DOUGH = ITEMS.register("dough", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
