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
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class PressingRecipeBuilder {
    private final Ingredient ingredient;
    private int inputCount = 1;
    private @Nullable ItemStackTemplate result;
    private PressRecipe.@Nullable FluidRef fluidRef;

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

    public PressingRecipeBuilder fluidResult(Fluid fluid, int amount) {
        this.fluidRef = new PressRecipe.FluidRef(
                net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid), amount);
        return this;
    }

    public PressingRecipeBuilder fluidResult(Identifier fluidId, int amount) {
        this.fluidRef = new PressRecipe.FluidRef(fluidId, amount);
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("pressing/" + name));
        output.accept(key, new PressRecipe(ingredient, inputCount,
                Optional.ofNullable(result), Optional.ofNullable(fluidRef)), null);
    }
}
