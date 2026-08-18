package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;

public class BottlingRecipeBuilder {

    private final Ingredient container;
    private PressRecipe.FluidRef fluid;
    private ItemStackTemplate result;
    private int priority = 0;

    private BottlingRecipeBuilder(Ingredient container) {
        this.container = container;
    }

    public static BottlingRecipeBuilder bottling(Ingredient container) {
        return new BottlingRecipeBuilder(container);
    }

    public BottlingRecipeBuilder fluid(Fluid fluid, int amount) {
        this.fluid = new PressRecipe.FluidRef(BuiltInRegistries.FLUID.getKey(fluid), amount);
        return this;
    }

    public BottlingRecipeBuilder result(ItemStackTemplate result) {
        this.result = result;
        return this;
    }

    public BottlingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("bottling/" + name));
        output.accept(key, new BottlingRecipe(container, fluid, result, priority), null);
    }
}
