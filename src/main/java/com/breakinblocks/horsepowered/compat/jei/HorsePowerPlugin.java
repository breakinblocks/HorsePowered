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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class HorsePowerPlugin implements IModPlugin {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "jei_plugin");

    public static final RecipeType<GrindstoneRecipe> GRINDING_TYPE =
            RecipeType.create(HorsePowerMod.MOD_ID, "grinding", GrindstoneRecipe.class);

    public static final RecipeType<ChoppingRecipe> CHOPPING_TYPE =
            RecipeType.create(HorsePowerMod.MOD_ID, "chopping", ChoppingRecipe.class);

    public static final RecipeType<PressRecipe> PRESSING_TYPE =
            RecipeType.create(HorsePowerMod.MOD_ID, "pressing", PressRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new HorsePowerGrindingCategory(guiHelper),
                new HPChoppingCategory(guiHelper),
                new HorsePowerPressCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        // Grinding recipes - unwrap from RecipeHolder
        List<GrindstoneRecipe> grindingRecipes = recipeManager.getAllRecipesFor(HPRecipes.GRINDING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(GRINDING_TYPE, grindingRecipes);

        // Chopping recipes - unwrap from RecipeHolder
        List<ChoppingRecipe> choppingRecipes = recipeManager.getAllRecipesFor(HPRecipes.CHOPPING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(CHOPPING_TYPE, choppingRecipes);

        // Pressing recipes - unwrap from RecipeHolder
        List<PressRecipe> pressingRecipes = recipeManager.getAllRecipesFor(HPRecipes.PRESSING_TYPE.get())
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

        // Chopping catalysts
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPING_BLOCK.get()), CHOPPING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPER.get()), CHOPPING_TYPE);

        // Pressing catalysts
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESS.get()), PRESSING_TYPE);
    }
}
