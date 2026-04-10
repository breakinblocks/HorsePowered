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

    public GrindstoneRecipe(Ingredient ingredient, ItemStackTemplate result,
                            Optional<ItemStackTemplate> secondary, int secondaryChance, int time) {
        super(ingredient, result);
        this.secondary = secondary.orElse(null);
        this.secondaryChance = Math.max(0, Math.min(100, secondaryChance));
        this.time = time;
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

    /**
     * Creates a new ItemStack from the secondary template.
     * Safe to call at runtime when item components are bound.
     */
    public ItemStack createSecondary() {
        return secondary != null ? secondary.create() : ItemStack.EMPTY;
    }

    public int getSecondaryChance() {
        return secondaryChance;
    }

    public int getTime() {
        return time;
    }

    // Codecs - use ItemStackTemplate.CODEC to avoid bound-component issues during recipe loading
    public static final MapCodec<GrindstoneRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(GrindstoneRecipe::getIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(GrindstoneRecipe::getResult),
                    ItemStackTemplate.CODEC.optionalFieldOf("secondary").forGetter(r -> Optional.ofNullable(r.getSecondaryTemplate())),
                    Codec.INT.optionalFieldOf("secondaryChance", 0).forGetter(GrindstoneRecipe::getSecondaryChance),
                    Codec.INT.fieldOf("time").forGetter(GrindstoneRecipe::getTime)
            ).apply(instance, GrindstoneRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GrindstoneRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, GrindstoneRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC, GrindstoneRecipe::getResult,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), r -> Optional.ofNullable(r.getSecondaryTemplate()),
            ByteBufCodecs.VAR_INT, GrindstoneRecipe::getSecondaryChance,
            ByteBufCodecs.VAR_INT, GrindstoneRecipe::getTime,
            GrindstoneRecipe::new
    );
}
