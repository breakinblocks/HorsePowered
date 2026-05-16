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
    private final RecipeTier tier;
    private final int priority;
    private final float hungerCost;

    public ChoppingRecipe(Ingredient ingredient, ItemStackTemplate result, int time, RecipeTier tier, int priority, float hungerCost) {
        super(ingredient, result);
        this.time = time;
        this.tier = tier;
        this.priority = priority;
        this.hungerCost = Math.max(0.0F, hungerCost);
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

    public RecipeTier getTier() {
        return tier;
    }

    public int getPriority() {
        return priority;
    }

    public float getHungerCost() {
        return hungerCost;
    }

    public static final MapCodec<ChoppingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ChoppingRecipe::getIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ChoppingRecipe::getResult),
                    Codec.INT.fieldOf("time").forGetter(ChoppingRecipe::getTime),
                    RecipeTier.CODEC.optionalFieldOf("tier", RecipeTier.ANY).forGetter(ChoppingRecipe::getTier),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(ChoppingRecipe::getPriority),
                    Codec.FLOAT.optionalFieldOf("hungerCost", 0.0F).forGetter(ChoppingRecipe::getHungerCost)
            ).apply(instance, ChoppingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChoppingRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
            int time = ByteBufCodecs.VAR_INT.decode(buf);
            RecipeTier tier = RecipeTier.STREAM_CODEC.decode(buf);
            int priority = ByteBufCodecs.VAR_INT.decode(buf);
            float hungerCost = ByteBufCodecs.FLOAT.decode(buf);
            return new ChoppingRecipe(ingredient, result, time, tier, priority, hungerCost);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ChoppingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getResult());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
            RecipeTier.STREAM_CODEC.encode(buf, recipe.getTier());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getPriority());
            ByteBufCodecs.FLOAT.encode(buf, recipe.getHungerCost());
        }
    };
}
