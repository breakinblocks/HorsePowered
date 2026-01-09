package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
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
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;

// TODO: Update to non-deprecated JEI API when available
@SuppressWarnings("removal")
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
                new HPChoppingCategory(guiHelper),
                new HPManualChoppingCategory(guiHelper),
                new HorsePowerPressCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Since 1.21.2, recipes are server-side only - use ServerLifecycleHooks to get RecipeManager
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            // In dedicated server multiplayer without integrated server, JEI handles syncing
            return;
        }
        RecipeManager recipeManager = server.getRecipeManager();

        // Grinding recipes - unwrap from RecipeHolder
        List<GrindstoneRecipe> grindingRecipes = recipeManager.recipeMap().byType(HPRecipes.GRINDING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(GRINDING_TYPE, grindingRecipes);

        // Chopping recipes - unwrap from RecipeHolder
        List<ChoppingRecipe> choppingRecipes = recipeManager.recipeMap().byType(HPRecipes.CHOPPING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(CHOPPING_TYPE, choppingRecipes);

        // Manual chopping uses the same recipes
        registration.addRecipes(MANUAL_CHOPPING_TYPE, choppingRecipes);

        // Pressing recipes - unwrap from RecipeHolder
        List<PressRecipe> pressingRecipes = recipeManager.recipeMap().byType(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
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
