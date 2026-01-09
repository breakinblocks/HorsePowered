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

public class GrindstoneRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final ItemStack secondary;
    private final int secondaryChance;
    private final int time;

    public GrindstoneRecipe(Ingredient ingredient, ItemStack result, ItemStack secondary, int secondaryChance, int time) {
        this.ingredient = ingredient;
        this.result = result;
        this.secondary = secondary;
        this.secondaryChance = Math.max(0, Math.min(100, secondaryChance));
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
    public RecipeSerializer<GrindstoneRecipe> getSerializer() {
        return HPRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<GrindstoneRecipe> getType() {
        return HPRecipes.GRINDING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.GRINDING_CATEGORY.get();
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

    public ItemStack getSecondary() {
        return secondary;
    }

    public int getSecondaryChance() {
        return secondaryChance;
    }

    public int getTime() {
        return time;
    }

    public static class Serializer implements RecipeSerializer<GrindstoneRecipe> {

        public static final MapCodec<GrindstoneRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(GrindstoneRecipe::getIngredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(GrindstoneRecipe::getResult),
                        ItemStack.OPTIONAL_CODEC.optionalFieldOf("secondary", ItemStack.EMPTY).forGetter(GrindstoneRecipe::getSecondary),
                        Codec.INT.optionalFieldOf("secondaryChance", 0).forGetter(GrindstoneRecipe::getSecondaryChance),
                        Codec.INT.fieldOf("time").forGetter(GrindstoneRecipe::getTime)
                ).apply(instance, GrindstoneRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, GrindstoneRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, GrindstoneRecipe::getIngredient,
                ItemStack.STREAM_CODEC, GrindstoneRecipe::getResult,
                ItemStack.OPTIONAL_STREAM_CODEC, GrindstoneRecipe::getSecondary,
                ByteBufCodecs.VAR_INT, GrindstoneRecipe::getSecondaryChance,
                ByteBufCodecs.VAR_INT, GrindstoneRecipe::getTime,
                GrindstoneRecipe::new
        );

        @Override
        public MapCodec<GrindstoneRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GrindstoneRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
