package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class GrindstoneRecipe extends BaseHPRecipe {

    private final @Nullable ItemStackTemplate secondary;
    private final int secondaryChance;
    private final int time;
    private final RecipeTier tier;
    private final int priority;
    private final float hungerCost;

    public GrindstoneRecipe(Ingredient ingredient, ItemStackTemplate result,
                            Optional<ItemStackTemplate> secondary, int secondaryChance, int time,
                            RecipeTier tier, int priority, float hungerCost) {
        super(ingredient, result);
        this.secondary = secondary.orElse(null);
        this.secondaryChance = Math.max(0, Math.min(100, secondaryChance));
        this.time = time;
        this.tier = tier;
        this.priority = priority;
        this.hungerCost = Math.max(0.0F, hungerCost);
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

    public @Nullable ItemStackTemplate getSecondaryTemplate() {
        return secondary;
    }

    public ItemStack createSecondary() {
        return secondary != null ? secondary.create() : ItemStack.EMPTY;
    }

    public int getSecondaryChance() {
        return secondaryChance;
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

    public static final MapCodec<GrindstoneRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(GrindstoneRecipe::getIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(GrindstoneRecipe::getResult),
                    ItemStackTemplate.CODEC.optionalFieldOf("secondary").forGetter(r -> Optional.ofNullable(r.getSecondaryTemplate())),
                    Codec.INT.optionalFieldOf("secondaryChance", 0).forGetter(GrindstoneRecipe::getSecondaryChance),
                    Codec.INT.fieldOf("time").forGetter(GrindstoneRecipe::getTime),
                    RecipeTier.CODEC.optionalFieldOf("tier", RecipeTier.ANY).forGetter(GrindstoneRecipe::getTier),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(GrindstoneRecipe::getPriority),
                    Codec.FLOAT.optionalFieldOf("hungerCost", 0.0F).forGetter(GrindstoneRecipe::getHungerCost)
            ).apply(instance, GrindstoneRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GrindstoneRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GrindstoneRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
            Optional<ItemStackTemplate> secondary = ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).decode(buf);
            int secondaryChance = ByteBufCodecs.VAR_INT.decode(buf);
            int time = ByteBufCodecs.VAR_INT.decode(buf);
            RecipeTier tier = RecipeTier.STREAM_CODEC.decode(buf);
            int priority = ByteBufCodecs.VAR_INT.decode(buf);
            float hungerCost = ByteBufCodecs.FLOAT.decode(buf);
            return new GrindstoneRecipe(ingredient, result, secondary, secondaryChance, time, tier, priority, hungerCost);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, GrindstoneRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getResult());
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).encode(buf, Optional.ofNullable(recipe.getSecondaryTemplate()));
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getSecondaryChance());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
            RecipeTier.STREAM_CODEC.encode(buf, recipe.getTier());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.getPriority());
            ByteBufCodecs.FLOAT.encode(buf, recipe.getHungerCost());
        }
    };
}
