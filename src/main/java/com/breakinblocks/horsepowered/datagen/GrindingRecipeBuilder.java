package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class GrindingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private @Nullable ItemStackTemplate secondary;
    private int secondaryChance;
    private int time = 12;

    private GrindingRecipeBuilder(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static GrindingRecipeBuilder grinding(Ingredient ingredient, ItemLike result, int count) {
        return new GrindingRecipeBuilder(ingredient, new ItemStackTemplate(result.asItem(), count));
    }

    public static GrindingRecipeBuilder grinding(Ingredient ingredient, ItemLike result) {
        return grinding(ingredient, result, 1);
    }

    public GrindingRecipeBuilder secondary(ItemLike item, int count, int chance) {
        this.secondary = new ItemStackTemplate(item.asItem(), count);
        this.secondaryChance = chance;
        return this;
    }

    public GrindingRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("grinding/" + name));
        output.accept(key, new GrindstoneRecipe(ingredient, result,
                Optional.ofNullable(secondary), secondaryChance, time), null);
    }
}
