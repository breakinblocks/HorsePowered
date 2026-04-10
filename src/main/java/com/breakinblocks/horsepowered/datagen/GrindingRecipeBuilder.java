package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
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

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("grinding/" + name);
        output.accept(id, new GrindstoneRecipe(ingredient, result, secondary, secondaryChance, time), null);
    }
}
