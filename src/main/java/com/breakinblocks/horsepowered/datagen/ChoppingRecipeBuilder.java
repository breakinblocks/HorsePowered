package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.RecipeTier;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class ChoppingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private int time = 1;
    private RecipeTier tier = RecipeTier.ANY;
    private int priority = 0;

    private ChoppingRecipeBuilder(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static ChoppingRecipeBuilder chopping(Ingredient ingredient, ItemLike result, int count) {
        return new ChoppingRecipeBuilder(ingredient, new ItemStackTemplate(result.asItem(), count));
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

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("chopping/" + name));
        output.accept(key, new ChoppingRecipe(ingredient, result, time, tier, priority), null);
    }
}
