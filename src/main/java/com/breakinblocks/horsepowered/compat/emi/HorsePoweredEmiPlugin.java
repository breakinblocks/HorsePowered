package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
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
    public static final EmiRecipeCategory DRYING = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "drying"),
            EmiStack.of(ModBlocks.DRYING_RACK.get()));
    public static final EmiRecipeCategory CRUSHING = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "crushing"),
            EmiStack.of(ModBlocks.GRANITE_ANVIL.get()));
    public static final EmiRecipeCategory TRAPPING = new EmiRecipeCategory(
            new ResourceLocation(Reference.MODID, "trapping"),
            EmiStack.of(ModBlocks.ANIMAL_TRAP.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(GRINDING);
        registry.addCategory(GRINDING_HAND);
        registry.addCategory(CHOPPING);
        registry.addCategory(CHOPPING_HAND);
        registry.addCategory(PRESSING);
        registry.addCategory(DRYING);
        registry.addCategory(CRUSHING);
        registry.addCategory(TRAPPING);

        registry.addWorkstation(GRINDING, EmiStack.of(ModBlocks.GRINDSTONE.get()));
        registry.addWorkstation(GRINDING_HAND, EmiStack.of(ModBlocks.HAND_GRINDSTONE.get()));
        registry.addWorkstation(CHOPPING, EmiStack.of(ModBlocks.CHOPPER.get()));
        registry.addWorkstation(CHOPPING_HAND, EmiStack.of(ModBlocks.CHOPPING_BLOCK.get()));
        registry.addWorkstation(PRESSING, EmiStack.of(ModBlocks.PRESS.get()));
        registry.addWorkstation(DRYING, EmiStack.of(ModBlocks.DRYING_RACK.get()));
        registry.addWorkstation(CRUSHING, EmiStack.of(ModBlocks.GRANITE_ANVIL.get()));
        registry.addWorkstation(TRAPPING, EmiStack.of(ModBlocks.ANIMAL_TRAP.get()));

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

        rm.getAllRecipesFor(HPRecipes.DRYING_TYPE.get())
                .forEach(r -> registry.addRecipe(new EmiDryingRecipe(DRYING, r)));

        Comparator<CrushingRecipe> crushOrder = Comparator
                .comparingInt(CrushingRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        rm.getAllRecipesFor(HPRecipes.CRUSHING_TYPE.get()).stream().sorted(crushOrder)
                .forEach(r -> registry.addRecipe(new EmiCrushingRecipe(CRUSHING, r)));

        Comparator<TrappingRecipe> trapOrder = Comparator
                .comparingInt(TrappingRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        rm.getAllRecipesFor(HPRecipes.TRAPPING_TYPE.get()).stream().sorted(trapOrder)
                .forEach(r -> registry.addRecipe(new EmiTrappingRecipe(TRAPPING, r)));
    }
}
