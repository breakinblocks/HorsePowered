package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.RecipeTier;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ChoppingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStack result;
    private int time = 1;
    private RecipeTier tier = RecipeTier.ANY;
    private int priority = 0;
    private float hungerCost = 0.0F;

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

    public ChoppingRecipeBuilder tier(RecipeTier tier) {
        this.tier = tier;
        return this;
    }

    public ChoppingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public ChoppingRecipeBuilder hungerCost(float hungerCost) {
        this.hungerCost = hungerCost;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("chopping/" + name);
        output.accept(id, new ChoppingRecipe(ingredient, result, time, tier, priority, hungerCost), null);
    }
}
