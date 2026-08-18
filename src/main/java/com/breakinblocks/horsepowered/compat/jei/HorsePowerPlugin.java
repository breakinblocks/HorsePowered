package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Comparator;
import java.util.List;

@JeiPlugin
public class HorsePowerPlugin implements IModPlugin {

    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "jei_plugin");

    public static final RecipeType<GrindstoneRecipe> GRINDING_TYPE =
            RecipeType.create(Reference.MODID, "grinding", GrindstoneRecipe.class);
    public static final RecipeType<GrindstoneRecipe> GRINDING_HAND_TYPE =
            RecipeType.create(Reference.MODID, "grinding_hand", GrindstoneRecipe.class);

    public static final RecipeType<ChoppingRecipe> CHOPPING_TYPE =
            RecipeType.create(Reference.MODID, "chopping", ChoppingRecipe.class);
    public static final RecipeType<ChoppingRecipe> CHOPPING_HAND_TYPE =
            RecipeType.create(Reference.MODID, "chopping_hand", ChoppingRecipe.class);

    public static final RecipeType<PressRecipe> PRESSING_TYPE =
            RecipeType.create(Reference.MODID, "pressing", PressRecipe.class);

    public static final RecipeType<DryingRackRecipe> DRYING_TYPE =
            RecipeType.create(Reference.MODID, "drying", DryingRackRecipe.class);

    public static final RecipeType<CrushingRecipe> CRUSHING_TYPE =
            RecipeType.create(Reference.MODID, "crushing", CrushingRecipe.class);

    public static final RecipeType<TrappingRecipe> TRAPPING_TYPE =
            RecipeType.create(Reference.MODID, "trapping", TrappingRecipe.class);

    public static final RecipeType<BottlingRecipe> BOTTLING_TYPE =
            RecipeType.create(Reference.MODID, "bottling", BottlingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new HorsePowerGrindingCategory(guiHelper, GRINDING_TYPE, ModBlocks.GRINDSTONE.get(), "gui.horsepowered.jei.grinding"),
                new HorsePowerGrindingCategory(guiHelper, GRINDING_HAND_TYPE, ModBlocks.HAND_GRINDSTONE.get(), "gui.horsepowered.jei.grinding_hand"),
                new HorsePowerChoppingCategory(guiHelper, CHOPPING_TYPE, ModBlocks.CHOPPER.get(), "gui.horsepowered.jei.chopping"),
                new HorsePowerChoppingCategory(guiHelper, CHOPPING_HAND_TYPE, ModBlocks.CHOPPING_BLOCK.get(), "gui.horsepowered.jei.chopping_hand"),
                new HorsePowerPressCategory(guiHelper),
                new HorsePowerDryingCategory(guiHelper),
                new HorsePowerCrushingCategory(guiHelper),
                new HorsePowerTrappingCategory(guiHelper),
                new HorsePowerBottlingCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        Comparator<GrindstoneRecipe> grindingOrder = Comparator
                .comparingInt(GrindstoneRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        Comparator<ChoppingRecipe> choppingOrder = Comparator
                .comparingInt(ChoppingRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        Comparator<PressRecipe> pressingOrder = Comparator
                .comparingInt(PressRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        Comparator<CrushingRecipe> crushingOrder = Comparator
                .comparingInt(CrushingRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());

        List<GrindstoneRecipe> allGrinding = recipeManager.getAllRecipesFor(HPRecipes.GRINDING_TYPE.get());
        registration.addRecipes(GRINDING_TYPE, allGrinding.stream()
                .filter(r -> r.getTier().allowsHorse()).sorted(grindingOrder).toList());
        registration.addRecipes(GRINDING_HAND_TYPE, allGrinding.stream()
                .filter(r -> r.getTier().allowsHand()).sorted(grindingOrder).toList());

        List<ChoppingRecipe> allChopping = recipeManager.getAllRecipesFor(HPRecipes.CHOPPING_TYPE.get());
        registration.addRecipes(CHOPPING_TYPE, allChopping.stream()
                .filter(r -> r.getTier().allowsHorse()).sorted(choppingOrder).toList());
        registration.addRecipes(CHOPPING_HAND_TYPE, allChopping.stream()
                .filter(r -> r.getTier().allowsHand()).sorted(choppingOrder).toList());

        registration.addRecipes(PRESSING_TYPE,
                recipeManager.getAllRecipesFor(HPRecipes.PRESSING_TYPE.get()).stream()
                        .sorted(pressingOrder).toList());

        registration.addRecipes(DRYING_TYPE,
                recipeManager.getAllRecipesFor(HPRecipes.DRYING_TYPE.get()));

        registration.addRecipes(CRUSHING_TYPE,
                recipeManager.getAllRecipesFor(HPRecipes.CRUSHING_TYPE.get()).stream()
                        .sorted(crushingOrder).toList());

        Comparator<TrappingRecipe> trapOrder = Comparator
                .comparingInt(TrappingRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        registration.addRecipes(TRAPPING_TYPE,
                recipeManager.getAllRecipesFor(HPRecipes.TRAPPING_TYPE.get()).stream()
                        .sorted(trapOrder).toList());

        Comparator<BottlingRecipe> bottleOrder = Comparator
                .comparingInt(BottlingRecipe::getPriority)
                .thenComparing(r -> r.getId().toString());
        registration.addRecipes(BOTTLING_TYPE,
                recipeManager.getAllRecipesFor(HPRecipes.BOTTLING_TYPE.get()).stream()
                        .filter(BottlingRecipe::isValid).sorted(bottleOrder).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.HAND_GRINDSTONE.get()), GRINDING_HAND_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GRINDSTONE.get()), GRINDING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPING_BLOCK.get()), CHOPPING_HAND_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPER.get()), CHOPPING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESS.get()), PRESSING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRYING_RACK.get()), DRYING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GRANITE_ANVIL.get()), CRUSHING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ANIMAL_TRAP.get()), TRAPPING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESS.get()), BOTTLING_TYPE);
    }
}
