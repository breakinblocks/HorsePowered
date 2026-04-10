package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ChoppingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStack result;
    private int time = 1;

    private ChoppingRecipeBuilder(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static ChoppingRecipeBuilder chopping(Ingredient ingredient, ItemLike result, int count) {
        return new ChoppingRecipeBuilder(ingredient, new ItemStack(result, count));
    }

    public static ChoppingRecipeBuilder chopping(Ingredient ingredient, ItemLike result) {
        return chopping(ingredient, result, 1);
    }

    public ChoppingRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("chopping/" + name);
        output.accept(id, new ChoppingRecipe(ingredient, result, time), null);
    }
}
