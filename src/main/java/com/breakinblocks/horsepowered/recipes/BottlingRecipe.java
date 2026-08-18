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
import net.neoforged.neoforge.fluids.FluidStack;

public class BottlingRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient container;
    private final FluidStack fluid;
    private final ItemStack result;
    private final int priority;

    public BottlingRecipe(Ingredient container, FluidStack fluid, ItemStack result, int priority) {
        this.container = container;
        this.fluid = fluid;
        this.result = result;
        this.priority = priority;
    }

    @Override
    public boolean matches(HPRecipeInput input, Level level) {
        return container.test(input.getItem(0));
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
        list.add(container);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HPRecipes.BOTTLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.BOTTLING_TYPE.get();
    }

    public Ingredient getContainer() {
        return container;
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isValid() {
        return !fluid.isEmpty() && !result.isEmpty() && container.getItems().length > 0;
    }

    public boolean matchesContainer(ItemStack stack) {
        return container.test(stack);
    }

    public boolean matchesResult(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, result);
    }

    public ItemStack getEmptyContainer() {
        ItemStack[] items = container.getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[0].copyWithCount(1);
    }

    public static class Serializer implements RecipeSerializer<BottlingRecipe> {

        public static final MapCodec<BottlingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("container").forGetter(BottlingRecipe::getContainer),
                        FluidStack.CODEC.fieldOf("fluid").forGetter(BottlingRecipe::getFluid),
                        ItemStack.CODEC.fieldOf("result").forGetter(BottlingRecipe::getResult),
                        Codec.INT.optionalFieldOf("priority", 0).forGetter(BottlingRecipe::getPriority)
                ).apply(instance, BottlingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, BottlingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, BottlingRecipe::getContainer,
                FluidStack.STREAM_CODEC, BottlingRecipe::getFluid,
                ItemStack.STREAM_CODEC, BottlingRecipe::getResult,
                ByteBufCodecs.VAR_INT, BottlingRecipe::getPriority,
                BottlingRecipe::new
        );

        @Override
        public MapCodec<BottlingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BottlingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
