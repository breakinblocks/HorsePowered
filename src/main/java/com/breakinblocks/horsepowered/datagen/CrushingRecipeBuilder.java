package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class CrushingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStack result;
    private int time = 1;
    private int priority = 0;
    private float hungerCost = 0.0F;

    private CrushingRecipeBuilder(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static CrushingRecipeBuilder crushing(Ingredient ingredient, ItemLike result, int count) {
        return new CrushingRecipeBuilder(ingredient, new ItemStack(result, count));
    }

    public static CrushingRecipeBuilder crushing(Ingredient ingredient, ItemLike result) {
        return crushing(ingredient, result, 1);
    }

    public CrushingRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public CrushingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public CrushingRecipeBuilder hungerCost(float hungerCost) {
        this.hungerCost = hungerCost;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("crushing/" + name);
        output.accept(id, new CrushingRecipe(ingredient, result, time, priority, hungerCost), null);
    }
}
