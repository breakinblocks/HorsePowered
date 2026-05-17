package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Comparator;
import java.util.List;

@EmiEntrypoint
public class HorsePoweredEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory GRINDING = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "grinding"),
            EmiStack.of(ModBlocks.GRINDSTONE.get()));
    public static final EmiRecipeCategory GRINDING_HAND = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "grinding_hand"),
            EmiStack.of(ModBlocks.HAND_GRINDSTONE.get()));
    public static final EmiRecipeCategory CHOPPING = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "chopping"),
            EmiStack.of(ModBlocks.CHOPPER.get()));
    public static final EmiRecipeCategory CHOPPING_HAND = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "chopping_hand"),
            EmiStack.of(ModBlocks.CHOPPING_BLOCK.get()));
    public static final EmiRecipeCategory PRESSING = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "pressing"),
            EmiStack.of(ModBlocks.PRESS.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(GRINDING);
        registry.addCategory(GRINDING_HAND);
        registry.addCategory(CHOPPING);
        registry.addCategory(CHOPPING_HAND);
        registry.addCategory(PRESSING);

        registry.addWorkstation(GRINDING, EmiStack.of(ModBlocks.GRINDSTONE.get()));
        registry.addWorkstation(GRINDING_HAND, EmiStack.of(ModBlocks.HAND_GRINDSTONE.get()));
        registry.addWorkstation(CHOPPING, EmiStack.of(ModBlocks.CHOPPER.get()));
        registry.addWorkstation(CHOPPING_HAND, EmiStack.of(ModBlocks.CHOPPING_BLOCK.get()));
        registry.addWorkstation(PRESSING, EmiStack.of(ModBlocks.PRESS.get()));

        RecipeManager rm = registry.getRecipeManager();

        Comparator<GrindstoneRecipe> grindOrder = Comparator
                .comparingInt(GrindstoneRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        Comparator<ChoppingRecipe> chopOrder = Comparator
                .comparingInt(ChoppingRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        Comparator<PressRecipe> pressOrder = Comparator
                .comparingInt(PressRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());

        List<GrindstoneRecipe> allGrinding = rm.getAllRecipesFor(HPRecipes.GRINDING_TYPE.get());
        allGrinding.stream().filter(r -> r.getTier().allowsHorse()).sorted(grindOrder)
                .forEach(r -> registry.addRecipe(new EmiGrindingRecipe(GRINDING, r)));
        allGrinding.stream().filter(r -> r.getTier().allowsHand()).sorted(grindOrder)
                .forEach(r -> registry.addRecipe(new EmiGrindingRecipe(GRINDING_HAND, r)));

        List<ChoppingRecipe> allChopping = rm.getAllRecipesFor(HPRecipes.CHOPPING_TYPE.get());
        allChopping.stream().filter(r -> r.getTier().allowsHorse()).sorted(chopOrder)
                .forEach(r -> registry.addRecipe(new EmiChoppingRecipe(CHOPPING, r)));
        allChopping.stream().filter(r -> r.getTier().allowsHand()).sorted(chopOrder)
                .forEach(r -> registry.addRecipe(new EmiChoppingRecipe(CHOPPING_HAND, r)));

        rm.getAllRecipesFor(HPRecipes.PRESSING_TYPE.get()).stream().sorted(pressOrder)
                .forEach(r -> registry.addRecipe(new EmiPressRecipe(PRESSING, r)));
    }
}
