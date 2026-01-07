package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.items.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class HPRecipeProvider extends RecipeProvider.Runner {

    public HPRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new HPRecipes(registries, output);
    }

    @Override
    public String getName() {
        return "Horse Powered Recipes";
    }

    private static class HPRecipes extends RecipeProvider {

        protected HPRecipes(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        protected void buildRecipes() {
            // Note: Guide book recipe is defined manually in guide.json
            // because it requires outputting guideme:guide with a custom component

            // Hand Grindstone
            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.HAND_GRINDSTONE.get())
                    .pattern("S S")
                    .pattern("SSS")
                    .pattern("P P")
                    .define('S', Items.STONE)
                    .define('P', Items.STICK)
                    .unlockedBy("has_stone", has(Items.STONE))
                    .save(this.output, recipeKey("crafting/hand_grindstone"));

            // Grindstone (horse-powered)
            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.GRINDSTONE.get())
                    .pattern("S S")
                    .pattern("SSS")
                    .pattern("LLL")
                    .define('S', Items.STONE)
                    .define('L', ItemTags.LOGS)
                    .unlockedBy("has_stone", has(Items.STONE))
                    .save(this.output, recipeKey("crafting/grindstone"));

            // Chopping Block
            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.CHOPPING_BLOCK.get())
                    .pattern("L")
                    .pattern("L")
                    .define('L', ItemTags.LOGS)
                    .unlockedBy("has_log", has(ItemTags.LOGS))
                    .save(this.output, recipeKey("crafting/chopping_block"));

            // Chopper (horse-powered)
            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.CHOPPER.get())
                    .pattern("I I")
                    .pattern("LLL")
                    .pattern("LLL")
                    .define('I', Items.IRON_INGOT)
                    .define('L', ItemTags.LOGS)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(this.output, recipeKey("crafting/chopper"));

            // Press
            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.PRESS.get())
                    .pattern("PPP")
                    .pattern("P P")
                    .pattern("PPP")
                    .define('P', ItemTags.PLANKS)
                    .unlockedBy("has_planks", has(ItemTags.PLANKS))
                    .save(this.output, recipeKey("crafting/press"));

            // Dough from flour + water bucket
            ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.FOOD, ModItems.DOUGH.get())
                    .requires(ModItems.FLOUR.get())
                    .requires(Items.WATER_BUCKET)
                    .unlockedBy("has_flour", has(ModItems.FLOUR.get()))
                    .save(this.output, recipeKey("crafting/dough"));

            // Dough smelting to bread
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.DOUGH.get()), RecipeCategory.FOOD, Items.BREAD, 0.35f, 200)
                    .unlockedBy("has_dough", has(ModItems.DOUGH.get()))
                    .save(this.output, recipeKey("smelting/dough_to_bread"));
        }

        private static ResourceKey<Recipe<?>> recipeKey(String path) {
            return ResourceKey.create(Registries.RECIPE, HorsePowerMod.id(path));
        }
    }
}
