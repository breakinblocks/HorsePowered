package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class DryingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStack result;
    private int time = 200;

    private DryingRecipeBuilder(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static DryingRecipeBuilder drying(Ingredient ingredient, ItemLike result, int count) {
        return new DryingRecipeBuilder(ingredient, new ItemStack(result, count));
    }

    public static DryingRecipeBuilder drying(Ingredient ingredient, ItemLike result) {
        return drying(ingredient, result, 1);
    }

    public DryingRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("drying/" + name);
        output.accept(id, new DryingRackRecipe(ingredient, result, time), null);
    }
}
