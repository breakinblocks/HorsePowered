package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.level.Level;

public class ChoppingRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int time;

    public ChoppingRecipe(Ingredient ingredient, ItemStack result, int time) {
        this.ingredient = ingredient;
        this.result = result;
        this.time = time;
    }

    @Override
    public boolean matches(HPRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(HPRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    // No longer part of Recipe interface in 1.21.11
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    // No longer part of Recipe interface in 1.21.11
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    // No longer part of Recipe interface in 1.21.11
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public RecipeSerializer<ChoppingRecipe> getSerializer() {
        return HPRecipes.CHOPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<ChoppingRecipe> getType() {
        return HPRecipes.CHOPPING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.CHOPPING_CATEGORY.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredient);
    }

    // Accessors
    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getTime() {
        return time;
    }

    public static class Serializer implements RecipeSerializer<ChoppingRecipe> {

        public static final MapCodec<ChoppingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(ChoppingRecipe::getIngredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ChoppingRecipe::getResult),
                        Codec.INT.fieldOf("time").forGetter(ChoppingRecipe::getTime)
                ).apply(instance, ChoppingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ChoppingRecipe::getIngredient,
                ItemStack.STREAM_CODEC, ChoppingRecipe::getResult,
                ByteBufCodecs.VAR_INT, ChoppingRecipe::getTime,
                ChoppingRecipe::new
        );

        @Override
        public MapCodec<ChoppingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
