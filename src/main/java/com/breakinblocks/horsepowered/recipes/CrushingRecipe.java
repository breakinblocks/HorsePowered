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

public class CrushingRecipe extends BaseHPRecipe {

    private final int time;
    private final int priority;
    private final float hungerCost;

    public CrushingRecipe(Ingredient ingredient, ItemStackTemplate result, int time, int priority, float hungerCost) {
        super(ingredient, result);
        this.time = time;
        this.priority = priority;
        this.hungerCost = Math.max(0.0F, hungerCost);
    }

    @Override
    public RecipeSerializer<CrushingRecipe> getSerializer() {
        return HPRecipes.CRUSHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CrushingRecipe> getType() {
        return HPRecipes.CRUSHING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.CRUSHING_CATEGORY.get();
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

    public static final MapCodec<CrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(CrushingRecipe::getIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(CrushingRecipe::getResult),
                    Codec.INT.fieldOf("time").forGetter(CrushingRecipe::getTime),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(CrushingRecipe::getPriority),
                    Codec.FLOAT.optionalFieldOf("hungerCost", 0.0F).forGetter(CrushingRecipe::getHungerCost)
            ).apply(instance, CrushingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CrushingRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
            int time = ByteBufCodecs.VAR_INT.decode(buf);
            int priority = ByteBufCodecs.VAR_INT.decode(buf);
            float hungerCost = ByteBufCodecs.FLOAT.decode(buf);
            return new CrushingRecipe(ingredient, result, time, priority, hungerCost);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CrushingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getResult());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getPriority());
            ByteBufCodecs.FLOAT.encode(buf, recipe.getHungerCost());
        }
    };
}
