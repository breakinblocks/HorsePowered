package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ChoppingRecipe extends BaseHPRecipe {

    private final int time;

    public ChoppingRecipe(Ingredient ingredient, ItemStackTemplate result, int time) {
        super(ingredient, result);
        this.time = time;
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

    public int getTime() {
        return time;
    }

    // Codecs - use ItemStackTemplate.CODEC to avoid bound-component issues during recipe loading
    public static final MapCodec<ChoppingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ChoppingRecipe::getIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ChoppingRecipe::getResult),
                    Codec.INT.fieldOf("time").forGetter(ChoppingRecipe::getTime)
            ).apply(instance, ChoppingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ChoppingRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC, ChoppingRecipe::getResult,
            ByteBufCodecs.VAR_INT, ChoppingRecipe::getTime,
            ChoppingRecipe::new
    );
}
