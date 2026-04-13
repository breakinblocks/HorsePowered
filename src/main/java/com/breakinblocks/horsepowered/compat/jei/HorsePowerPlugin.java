package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.events.HPDatapackSync;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.List;

@JeiPlugin
public class HorsePowerPlugin implements IModPlugin {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "jei_plugin");

    public static final RecipeType<GrindstoneRecipe> GRINDING_TYPE =
            RecipeType.create(HorsePowerMod.MOD_ID, "grinding", GrindstoneRecipe.class);

    public static final RecipeType<ChoppingRecipe> CHOPPING_TYPE =
            RecipeType.create(HorsePowerMod.MOD_ID, "chopping", ChoppingRecipe.class);

    public static final RecipeType<ChoppingRecipe> MANUAL_CHOPPING_TYPE =
            RecipeType.create(HorsePowerMod.MOD_ID, "manual_chopping", ChoppingRecipe.class);

    public static final RecipeType<PressRecipe> PRESSING_TYPE =
            RecipeType.create(HorsePowerMod.MOD_ID, "pressing", PressRecipe.class);

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new HorsePowerGrindingCategory(guiHelper),
                new HorsePowerChoppingCategory(guiHelper),
                new HorsePowerManualChoppingCategory(guiHelper),
                new HorsePowerPressCategory(guiHelper)
        );
    }

    // Recipes are captured from the server's sync into HPDatapackSync#clientRecipes
    // (we opt in via OnDatapackSyncEvent#sendRecipes on the server side).
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeMap recipeMap = HPDatapackSync.getClientRecipes();
        if (recipeMap == null) return;

        List<GrindstoneRecipe> grindingRecipes = recipeMap.byType(HPRecipes.GRINDING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        registration.addRecipes(GRINDING_TYPE, grindingRecipes);

        List<ChoppingRecipe> choppingRecipes = recipeMap.byType(HPRecipes.CHOPPING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        registration.addRecipes(CHOPPING_TYPE, choppingRecipes);
        registration.addRecipes(MANUAL_CHOPPING_TYPE, choppingRecipes);

        List<PressRecipe> pressingRecipes = recipeMap.byType(HPRecipes.PRESSING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        registration.addRecipes(PRESSING_TYPE, pressingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Grinding catalysts
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.HAND_GRINDSTONE.get()), GRINDING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GRINDSTONE.get()), GRINDING_TYPE);

        // Chopping catalysts - manual chopping block shows axe, horse chopper doesn't
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPING_BLOCK.get()), MANUAL_CHOPPING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPER.get()), CHOPPING_TYPE);

        // Pressing catalysts
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESS.get()), PRESSING_TYPE);
    }
}
