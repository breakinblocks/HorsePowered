package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class DryingRecipeBuilder {

    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private int time = 200;

    private DryingRecipeBuilder(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static DryingRecipeBuilder drying(Ingredient ingredient, ItemLike result, int count) {
        return new DryingRecipeBuilder(ingredient, new ItemStackTemplate(result.asItem(), count));
    }

    public static DryingRecipeBuilder drying(Ingredient ingredient, ItemLike result) {
        return drying(ingredient, result, 1);
    }

    public DryingRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("drying/" + name));
        output.accept(key, new DryingRackRecipe(ingredient, result, time), null);
    }
}
