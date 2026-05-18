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

public class CrushingRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int time;
    private final int priority;
    private final float hungerCost;

    public CrushingRecipe(Ingredient ingredient, ItemStack result, int time, int priority, float hungerCost) {
        this.ingredient = ingredient;
        this.result = result;
        this.time = time;
        this.priority = priority;
        this.hungerCost = Math.max(0.0F, hungerCost);
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
        return HPRecipes.CRUSHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.CRUSHING_TYPE.get();
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

    public int getPriority() {
        return priority;
    }

    public float getHungerCost() {
        return hungerCost;
    }

    public static class Serializer implements RecipeSerializer<CrushingRecipe> {

        public static final MapCodec<CrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CrushingRecipe::getIngredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CrushingRecipe::getResult),
                        Codec.INT.fieldOf("time").forGetter(CrushingRecipe::getTime),
                        Codec.INT.optionalFieldOf("priority", 0).forGetter(CrushingRecipe::getPriority),
                        Codec.FLOAT.optionalFieldOf("hungerCost", 0.0F).forGetter(CrushingRecipe::getHungerCost)
                ).apply(instance, CrushingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public CrushingRecipe decode(RegistryFriendlyByteBuf buf) {
                Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                int time = ByteBufCodecs.VAR_INT.decode(buf);
                int priority = ByteBufCodecs.VAR_INT.decode(buf);
                float hungerCost = ByteBufCodecs.FLOAT.decode(buf);
                return new CrushingRecipe(ingredient, result, time, priority, hungerCost);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, CrushingRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
                ItemStack.STREAM_CODEC.encode(buf, recipe.getResult());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getPriority());
                ByteBufCodecs.FLOAT.encode(buf, recipe.getHungerCost());
            }
        };

        @Override
        public MapCodec<CrushingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
