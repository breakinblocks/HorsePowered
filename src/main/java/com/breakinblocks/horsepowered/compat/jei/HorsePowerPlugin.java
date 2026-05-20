package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.events.HPDatapackSync;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.Comparator;
import java.util.List;

@JeiPlugin
public class HorsePowerPlugin implements IModPlugin {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "jei_plugin");

    public static final IRecipeType<GrindstoneRecipe> GRINDING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "grinding", GrindstoneRecipe.class);

    public static final IRecipeType<GrindstoneRecipe> MANUAL_GRINDING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "manual_grinding", GrindstoneRecipe.class);

    public static final IRecipeType<ChoppingRecipe> CHOPPING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "chopping", ChoppingRecipe.class);

    public static final IRecipeType<ChoppingRecipe> MANUAL_CHOPPING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "manual_chopping", ChoppingRecipe.class);

    public static final IRecipeType<PressRecipe> PRESSING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "pressing", PressRecipe.class);

    public static final IRecipeType<DryingRackRecipe> DRYING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "drying", DryingRackRecipe.class);

    public static final IRecipeType<CrushingRecipe> CRUSHING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "crushing", CrushingRecipe.class);

    public static final IRecipeType<TrappingRecipe> TRAPPING_TYPE =
            IRecipeType.create(HorsePowerMod.MOD_ID, "trapping", TrappingRecipe.class);

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new HorsePowerGrindingCategory(guiHelper),
                new HorsePowerManualGrindingCategory(guiHelper),
                new HorsePowerChoppingCategory(guiHelper),
                new HorsePowerManualChoppingCategory(guiHelper),
                new HorsePowerPressCategory(guiHelper),
                new HorsePowerDryingCategory(guiHelper),
                new HorsePowerCrushingCategory(guiHelper),
                new HorsePowerTrappingCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeMap recipeMap = HPDatapackSync.getClientRecipes();
        if (recipeMap == null) return;

        Comparator<GrindstoneRecipe> grindOrder = Comparator.comparingInt(GrindstoneRecipe::getPriority);
        Comparator<ChoppingRecipe> chopOrder = Comparator.comparingInt(ChoppingRecipe::getPriority);
        Comparator<PressRecipe> pressOrder = Comparator.comparingInt(PressRecipe::getPriority);

        List<GrindstoneRecipe> allGrinding = recipeMap.byType(HPRecipes.GRINDING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        registration.addRecipes(GRINDING_TYPE, allGrinding.stream()
                .filter(r -> r.getTier().allowsHorse()).sorted(grindOrder).toList());
        registration.addRecipes(MANUAL_GRINDING_TYPE, allGrinding.stream()
                .filter(r -> r.getTier().allowsHand()).sorted(grindOrder).toList());

        List<ChoppingRecipe> allChopping = recipeMap.byType(HPRecipes.CHOPPING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        registration.addRecipes(CHOPPING_TYPE, allChopping.stream()
                .filter(r -> r.getTier().allowsHorse()).sorted(chopOrder).toList());
        registration.addRecipes(MANUAL_CHOPPING_TYPE, allChopping.stream()
                .filter(r -> r.getTier().allowsHand()).sorted(chopOrder).toList());

        registration.addRecipes(PRESSING_TYPE, recipeMap.byType(HPRecipes.PRESSING_TYPE.get())
                .stream().map(RecipeHolder::value).sorted(pressOrder).toList());

        registration.addRecipes(DRYING_TYPE, recipeMap.byType(HPRecipes.DRYING_TYPE.get())
                .stream().map(RecipeHolder::value).toList());

        Comparator<CrushingRecipe> crushOrder = Comparator.comparingInt(CrushingRecipe::getPriority);
        registration.addRecipes(CRUSHING_TYPE, recipeMap.byType(HPRecipes.CRUSHING_TYPE.get())
                .stream().map(RecipeHolder::value).sorted(crushOrder).toList());

        Comparator<TrappingRecipe> trapOrder = Comparator.comparingInt(TrappingRecipe::getPriority);
        registration.addRecipes(TRAPPING_TYPE, recipeMap.byType(HPRecipes.TRAPPING_TYPE.get())
                .stream().map(RecipeHolder::value).sorted(trapOrder).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(GRINDING_TYPE, new ItemStack(ModBlocks.GRINDSTONE.get()));
        registration.addCraftingStation(MANUAL_GRINDING_TYPE, new ItemStack(ModBlocks.HAND_GRINDSTONE.get()));

        registration.addCraftingStation(MANUAL_CHOPPING_TYPE, new ItemStack(ModBlocks.CHOPPING_BLOCK.get()));
        registration.addCraftingStation(CHOPPING_TYPE, new ItemStack(ModBlocks.CHOPPER.get()));

        registration.addCraftingStation(PRESSING_TYPE, new ItemStack(ModBlocks.PRESS.get()));

        registration.addCraftingStation(DRYING_TYPE, new ItemStack(ModBlocks.DRYING_RACK.get()));

        registration.addCraftingStation(CRUSHING_TYPE, new ItemStack(ModBlocks.GRANITE_ANVIL.get()));

        registration.addCraftingStation(TRAPPING_TYPE, new ItemStack(ModBlocks.ANIMAL_TRAP.get()));
    }
}
