package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class PressingRecipeBuilder {
    private final Ingredient ingredient;
    private int inputCount = 1;
    private ItemStack result = ItemStack.EMPTY;
    private FluidStack fluidResult = FluidStack.EMPTY;
    private int priority = 0;

    private PressingRecipeBuilder(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public static PressingRecipeBuilder pressing(Ingredient ingredient) {
        return new PressingRecipeBuilder(ingredient);
    }

    public PressingRecipeBuilder inputCount(int count) {
        this.inputCount = count;
        return this;
    }

    public PressingRecipeBuilder result(ItemLike item, int count) {
        this.result = new ItemStack(item, count);
        return this;
    }

    public PressingRecipeBuilder fluidResult(Fluid fluid, int amount) {
        Holder<Fluid> holder = BuiltInRegistries.FLUID.wrapAsHolder(fluid);
        this.fluidResult = new FluidStack(holder, amount);
        return this;
    }

    public PressingRecipeBuilder fluidResult(ResourceLocation fluidId, int amount) {
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        return fluidResult(fluid, amount);
    }

    public PressingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("pressing/" + name);
        output.accept(id, new PressRecipe(ingredient, inputCount, result, fluidResult, priority), null);
    }
}
