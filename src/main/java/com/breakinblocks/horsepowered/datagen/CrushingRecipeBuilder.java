package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class CrushingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private int time = 1;
    private int priority = 0;
    private float hungerCost = 0.0F;

    private CrushingRecipeBuilder(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static CrushingRecipeBuilder crushing(Ingredient ingredient, ItemLike result, int count) {
        return new CrushingRecipeBuilder(ingredient, new ItemStackTemplate(result.asItem(), count));
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
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("crushing/" + name));
        output.accept(key, new CrushingRecipe(ingredient, result, time, priority, hungerCost), null);
    }
}
