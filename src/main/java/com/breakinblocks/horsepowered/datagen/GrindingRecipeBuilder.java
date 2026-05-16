package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.RecipeTier;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class GrindingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStack result;
    private ItemStack secondary = ItemStack.EMPTY;
    private int secondaryChance;
    private int time = 12;
    private RecipeTier tier = RecipeTier.ANY;
    private int priority = 0;
    private float hungerCost = 0.0F;

    private GrindingRecipeBuilder(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static GrindingRecipeBuilder grinding(Ingredient ingredient, ItemLike result, int count) {
        return new GrindingRecipeBuilder(ingredient, new ItemStack(result, count));
    }

    public static GrindingRecipeBuilder grinding(Ingredient ingredient, ItemLike result) {
        return grinding(ingredient, result, 1);
    }

    public GrindingRecipeBuilder secondary(ItemLike item, int count, int chance) {
        this.secondary = new ItemStack(item, count);
        this.secondaryChance = chance;
        return this;
    }

    public GrindingRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public GrindingRecipeBuilder tier(RecipeTier tier) {
        this.tier = tier;
        return this;
    }

    public GrindingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public GrindingRecipeBuilder hungerCost(float hungerCost) {
        this.hungerCost = hungerCost;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("grinding/" + name);
        output.accept(id, new GrindstoneRecipe(ingredient, result, secondary, secondaryChance, time, tier, priority, hungerCost), null);
    }
}
