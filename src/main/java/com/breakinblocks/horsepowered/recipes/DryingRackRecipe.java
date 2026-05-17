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

public class DryingRackRecipe extends BaseHPRecipe {

    private final int time;

    public DryingRackRecipe(Ingredient ingredient, ItemStackTemplate result, int time) {
        super(ingredient, result);
        this.time = Math.max(1, time);
    }

    @Override
    public RecipeSerializer<DryingRackRecipe> getSerializer() {
        return HPRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<DryingRackRecipe> getType() {
        return HPRecipes.DRYING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.DRYING_CATEGORY.get();
    }

    public int getTime() {
        return time;
    }

    public static final MapCodec<DryingRackRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(DryingRackRecipe::getIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(DryingRackRecipe::getResult),
                    Codec.INT.fieldOf("time").forGetter(DryingRackRecipe::getTime)
            ).apply(instance, DryingRackRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DryingRackRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
            int time = ByteBufCodecs.VAR_INT.decode(buf);
            return new DryingRackRecipe(ingredient, result, time);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, DryingRackRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getResult());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
        }
    };
}
