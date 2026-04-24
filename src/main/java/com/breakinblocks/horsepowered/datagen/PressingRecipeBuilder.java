package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class PressingRecipeBuilder {
    private final Ingredient ingredient;
    private int inputCount = 1;
    private @Nullable SizedFluidIngredient fluidInput;
    private @Nullable ItemStackTemplate result;
    private PressRecipe.@Nullable FluidRef fluidRef;
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
        this.result = new ItemStackTemplate(item.asItem(), count);
        return this;
    }

    public PressingRecipeBuilder fluidInput(SizedFluidIngredient input) {
        this.fluidInput = input;
        return this;
    }

    public PressingRecipeBuilder fluidInput(Fluid fluid, int amount) {
        this.fluidInput = new SizedFluidIngredient(FluidIngredient.of(fluid), amount);
        return this;
    }

    public PressingRecipeBuilder fluidResult(Fluid fluid, int amount) {
        this.fluidRef = new PressRecipe.FluidRef(
                net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid), amount);
        return this;
    }

    public PressingRecipeBuilder fluidResult(Identifier fluidId, int amount) {
        this.fluidRef = new PressRecipe.FluidRef(fluidId, amount);
        return this;
    }

    public PressingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("pressing/" + name));
        output.accept(key, new PressRecipe(ingredient, inputCount,
                Optional.ofNullable(fluidInput),
                Optional.ofNullable(result), Optional.ofNullable(fluidRef), priority), null);
    }
}
