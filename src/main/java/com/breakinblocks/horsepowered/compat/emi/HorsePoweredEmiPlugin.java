package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Comparator;
import java.util.List;

@EmiEntrypoint
public class HorsePoweredEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory GRINDING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "grinding"),
            EmiStack.of(ModBlocks.GRINDSTONE.get()));
    public static final EmiRecipeCategory MANUAL_GRINDING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "manual_grinding"),
            EmiStack.of(ModBlocks.HAND_GRINDSTONE.get()));
    public static final EmiRecipeCategory CHOPPING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "chopping"),
            EmiStack.of(ModBlocks.CHOPPER.get()));
    public static final EmiRecipeCategory MANUAL_CHOPPING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "manual_chopping"),
            EmiStack.of(ModBlocks.CHOPPING_BLOCK.get()));
    public static final EmiRecipeCategory PRESSING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "pressing"),
            EmiStack.of(ModBlocks.PRESS.get()));
    public static final EmiRecipeCategory DRYING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "drying"),
            EmiStack.of(ModBlocks.DRYING_RACK.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(GRINDING);
        registry.addCategory(MANUAL_GRINDING);
        registry.addCategory(CHOPPING);
        registry.addCategory(MANUAL_CHOPPING);
        registry.addCategory(PRESSING);
        registry.addCategory(DRYING);

        registry.addWorkstation(GRINDING, EmiStack.of(ModBlocks.GRINDSTONE.get()));
        registry.addWorkstation(MANUAL_GRINDING, EmiStack.of(ModBlocks.HAND_GRINDSTONE.get()));
        registry.addWorkstation(CHOPPING, EmiStack.of(ModBlocks.CHOPPER.get()));
        registry.addWorkstation(MANUAL_CHOPPING, EmiStack.of(ModBlocks.CHOPPING_BLOCK.get()));
        registry.addWorkstation(PRESSING, EmiStack.of(ModBlocks.PRESS.get()));
        registry.addWorkstation(DRYING, EmiStack.of(ModBlocks.DRYING_RACK.get()));

        RecipeManager rm = registry.getRecipeManager();

        Comparator<GrindstoneRecipe> grindOrder = Comparator.comparingInt(GrindstoneRecipe::getPriority);
        Comparator<ChoppingRecipe> chopOrder = Comparator.comparingInt(ChoppingRecipe::getPriority);
        Comparator<PressRecipe> pressOrder = Comparator.comparingInt(PressRecipe::getPriority);

        List<GrindstoneRecipe> allGrinding = rm.getAllRecipesFor(HPRecipes.GRINDING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        allGrinding.stream().filter(r -> r.getTier().allowsHorse()).sorted(grindOrder)
                .forEach(r -> registry.addRecipe(new EmiGrindingRecipe(GRINDING, r)));
        allGrinding.stream().filter(r -> r.getTier().allowsHand()).sorted(grindOrder)
                .forEach(r -> registry.addRecipe(new EmiGrindingRecipe(MANUAL_GRINDING, r)));

        List<ChoppingRecipe> allChopping = rm.getAllRecipesFor(HPRecipes.CHOPPING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        allChopping.stream().filter(r -> r.getTier().allowsHorse()).sorted(chopOrder)
                .forEach(r -> registry.addRecipe(new EmiChoppingRecipe(CHOPPING, r)));
        allChopping.stream().filter(r -> r.getTier().allowsHand()).sorted(chopOrder)
                .forEach(r -> registry.addRecipe(new EmiChoppingRecipe(MANUAL_CHOPPING, r)));

        rm.getAllRecipesFor(HPRecipes.PRESSING_TYPE.get()).stream()
                .map(RecipeHolder::value).sorted(pressOrder)
                .forEach(r -> registry.addRecipe(new EmiPressRecipe(PRESSING, r)));

        rm.getAllRecipesFor(HPRecipes.DRYING_TYPE.get()).stream()
                .map(RecipeHolder::value)
                .forEach(r -> registry.addRecipe(new EmiDryingRecipe(DRYING, r)));
    }
}
