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
import net.neoforged.neoforge.fluids.FluidStack;

public class PressRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient ingredient;
    private final int inputCount;
    private final ItemStack result;
    private final FluidStack fluidResult;

    public PressRecipe(Ingredient ingredient, int inputCount, ItemStack result, FluidStack fluidResult) {
        this.ingredient = ingredient;
        this.inputCount = inputCount;
        this.result = result;
        this.fluidResult = fluidResult;
    }

    @Override
    public boolean matches(HPRecipeInput input, Level level) {
        ItemStack inputItem = input.getItem(0);
        return ingredient.test(inputItem) && inputItem.getCount() >= inputCount;
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
    public RecipeSerializer<PressRecipe> getSerializer() {
        return HPRecipes.PRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<PressRecipe> getType() {
        return HPRecipes.PRESSING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.PRESSING_CATEGORY.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    // Accessors
    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStack getResult() {
        return result;
    }

    public FluidStack getFluidResult() {
        return fluidResult;
    }

    public boolean hasFluidOutput() {
        return !fluidResult.isEmpty();
    }

    public static class Serializer implements RecipeSerializer<PressRecipe> {

        public static final MapCodec<PressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(PressRecipe::getIngredient),
                        Codec.INT.optionalFieldOf("inputCount", 1).forGetter(PressRecipe::getInputCount),
                        ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(PressRecipe::getResult),
                        FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluidResult", FluidStack.EMPTY).forGetter(PressRecipe::getFluidResult)
                ).apply(instance, PressRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, PressRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, PressRecipe::getIngredient,
                ByteBufCodecs.VAR_INT, PressRecipe::getInputCount,
                ItemStack.OPTIONAL_STREAM_CODEC, PressRecipe::getResult,
                FluidStack.OPTIONAL_STREAM_CODEC, PressRecipe::getFluidResult,
                PressRecipe::new
        );

        @Override
        public MapCodec<PressRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PressRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
