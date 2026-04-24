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

public class GrindstoneRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final ItemStack secondary;
    private final int secondaryChance;
    private final int time;
    private final RecipeTier tier;
    private final int priority;

    public GrindstoneRecipe(Ingredient ingredient, ItemStack result, ItemStack secondary, int secondaryChance, int time, RecipeTier tier, int priority) {
        this.ingredient = ingredient;
        this.result = result;
        this.secondary = secondary;
        this.secondaryChance = Math.max(0, Math.min(100, secondaryChance));
        this.time = time;
        this.tier = tier;
        this.priority = priority;
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
        return HPRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.GRINDING_TYPE.get();
    }

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

    public RecipeTier getTier() {
        return tier;
    }

    public int getPriority() {
        return priority;
    }

    public static class Serializer implements RecipeSerializer<GrindstoneRecipe> {

        public static final MapCodec<GrindstoneRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(GrindstoneRecipe::getIngredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(GrindstoneRecipe::getResult),
                        ItemStack.OPTIONAL_CODEC.optionalFieldOf("secondary", ItemStack.EMPTY).forGetter(GrindstoneRecipe::getSecondary),
                        Codec.INT.optionalFieldOf("secondaryChance", 0).forGetter(GrindstoneRecipe::getSecondaryChance),
                        Codec.INT.fieldOf("time").forGetter(GrindstoneRecipe::getTime),
                        RecipeTier.CODEC.optionalFieldOf("tier", RecipeTier.ANY).forGetter(GrindstoneRecipe::getTier),
                        Codec.INT.optionalFieldOf("priority", 0).forGetter(GrindstoneRecipe::getPriority)
                ).apply(instance, GrindstoneRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, GrindstoneRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public GrindstoneRecipe decode(RegistryFriendlyByteBuf buf) {
                Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                ItemStack secondary = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int secondaryChance = ByteBufCodecs.VAR_INT.decode(buf);
                int time = ByteBufCodecs.VAR_INT.decode(buf);
                RecipeTier tier = RecipeTier.STREAM_CODEC.decode(buf);
                int priority = ByteBufCodecs.VAR_INT.decode(buf);
                return new GrindstoneRecipe(ingredient, result, secondary, secondaryChance, time, tier, priority);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, GrindstoneRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
                ItemStack.STREAM_CODEC.encode(buf, recipe.getResult());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.getSecondary());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getSecondaryChance());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
                RecipeTier.STREAM_CODEC.encode(buf, recipe.getTier());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getPriority());
            }
        };

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
