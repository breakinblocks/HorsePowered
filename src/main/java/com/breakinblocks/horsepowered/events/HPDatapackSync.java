package com.breakinblocks.horsepowered.events;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jspecify.annotations.Nullable;

// NeoForge 26.1 only sends recipe data to clients for types explicitly requested
// via OnDatapackSyncEvent#sendRecipes. Without this the client never sees our
// custom recipes and JEI / any client-side lookup is empty.
@EventBusSubscriber(modid = HorsePowerMod.MOD_ID)
public class HPDatapackSync {

    private static volatile @Nullable RecipeMap clientRecipes;

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(
                HPRecipes.GRINDING_TYPE.get(),
                HPRecipes.CHOPPING_TYPE.get(),
                HPRecipes.PRESSING_TYPE.get(),
                HPRecipes.DRYING_TYPE.get(),
                HPRecipes.CRUSHING_TYPE.get(),
                HPRecipes.TRAPPING_TYPE.get(),
                HPRecipes.BOTTLING_TYPE.get());
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        clientRecipes = event.getRecipeMap();
    }

    public static @Nullable RecipeMap getClientRecipes() {
        return clientRecipes;
    }
}
