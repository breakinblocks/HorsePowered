package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class BottlingRecipeBuilder {

    private final Ingredient container;
    private FluidStack fluid = FluidStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;
    private int priority = 0;

    private BottlingRecipeBuilder(Ingredient container) {
        this.container = container;
    }

    public static BottlingRecipeBuilder bottling(Ingredient container) {
        return new BottlingRecipeBuilder(container);
    }

    public BottlingRecipeBuilder fluid(Fluid fluid, int amount) {
        this.fluid = new FluidStack(fluid, amount);
        return this;
    }

    public BottlingRecipeBuilder result(ItemStack result) {
        this.result = result;
        return this;
    }

    public BottlingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("bottling/" + name);
        output.accept(id, new BottlingRecipe(container, fluid, result, priority), null);
    }
}
