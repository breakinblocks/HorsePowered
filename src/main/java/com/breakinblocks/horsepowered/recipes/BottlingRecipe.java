package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

public class BottlingRecipe extends BaseHPRecipe {

    private final PressRecipe.FluidRef fluidRef;
    private final int priority;

    public BottlingRecipe(Ingredient container, PressRecipe.FluidRef fluidRef,
                          ItemStackTemplate result, int priority) {
        super(container, result);
        this.fluidRef = fluidRef;
        this.priority = priority;
    }

    @Override
    public RecipeSerializer<BottlingRecipe> getSerializer() {
        return HPRecipes.BOTTLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<BottlingRecipe> getType() {
        return HPRecipes.BOTTLING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.BOTTLING_CATEGORY.get();
    }

    public Ingredient getContainer() {
        return ingredient;
    }

    public FluidStack getFluid() {
        return fluidRef.create();
    }

    public PressRecipe.FluidRef getFluidRef() {
        return fluidRef;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isValid() {
        return !getFluid().isEmpty() && !createResult().isEmpty() && !ingredient.isEmpty();
    }

    public boolean matchesContainer(ItemStack stack) {
        return ingredient.test(stack);
    }

    public boolean matchesResult(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, createResult());
    }

    public ItemStack getEmptyContainer() {
        return ingredient.getValues().stream()
                .findFirst()
                .map(Holder::value)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    public static final MapCodec<BottlingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("container").forGetter(BottlingRecipe::getContainer),
                    PressRecipe.FluidRef.CODEC.fieldOf("fluid").forGetter(BottlingRecipe::getFluidRef),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(BottlingRecipe::getPriority)
            ).apply(instance, BottlingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BottlingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, BottlingRecipe::getContainer,
            PressRecipe.FluidRef.STREAM_CODEC, BottlingRecipe::getFluidRef,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.VAR_INT, BottlingRecipe::getPriority,
            BottlingRecipe::new
    );
}
