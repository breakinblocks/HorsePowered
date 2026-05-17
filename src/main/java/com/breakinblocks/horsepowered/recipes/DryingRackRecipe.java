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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class DryingRackRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int time;

    public DryingRackRecipe(Ingredient ingredient, ItemStack result, int time) {
        this.ingredient = ingredient;
        this.result = result;
        this.time = Math.max(1, time);
    }

    @Override
    public boolean matches(HPRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(HPRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HPRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.DRYING_TYPE.get();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getTime() {
        return time;
    }

    public static class Serializer implements RecipeSerializer<DryingRackRecipe> {

        public static final MapCodec<DryingRackRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DryingRackRecipe::getIngredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DryingRackRecipe::getResult),
                        Codec.INT.fieldOf("time").forGetter(DryingRackRecipe::getTime)
                ).apply(instance, DryingRackRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public DryingRackRecipe decode(RegistryFriendlyByteBuf buf) {
                Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                int time = ByteBufCodecs.VAR_INT.decode(buf);
                return new DryingRackRecipe(ingredient, result, time);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, DryingRackRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
                ItemStack.STREAM_CODEC.encode(buf, recipe.getResult());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
            }
        };

        @Override
        public MapCodec<DryingRackRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
