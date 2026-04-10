package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public class PressRecipe extends BaseHPRecipe {

    /**
     * Deferred fluid reference that avoids creating a FluidStack during recipe loading
     * (when fluid components aren't bound yet). The FluidStack is created lazily at runtime.
     */
    public record FluidRef(Identifier fluidId, int amount) {
        public static final Codec<FluidRef> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("id").forGetter(FluidRef::fluidId),
                        ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidRef::amount)
                ).apply(instance, FluidRef::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidRef> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, FluidRef::fluidId,
                ByteBufCodecs.VAR_INT, FluidRef::amount,
                FluidRef::new
        );

        /** Creates a FluidStack at runtime when fluid components are bound. */
        public FluidStack create() {
            return new FluidStack(BuiltInRegistries.FLUID.getValue(fluidId), amount);
        }

        public static FluidRef from(FluidStack stack) {
            return new FluidRef(BuiltInRegistries.FLUID.getKey(stack.getFluid()), stack.getAmount());
        }
    }

    private final int inputCount;
    private final Optional<FluidRef> fluidRef;

    public PressRecipe(Ingredient ingredient, int inputCount,
                       Optional<ItemStackTemplate> result, Optional<FluidRef> fluidRef) {
        super(ingredient, result.orElse(null));
        this.inputCount = inputCount;
        this.fluidRef = fluidRef;
    }

    @Override
    public boolean matches(HPRecipeInput input, Level level) {
        ItemStack inputItem = input.getItem(0);
        return ingredient.test(inputItem) && inputItem.getCount() >= inputCount;
    }

    @Override
    public RecipeSerializer<PressRecipe> getSerializer() {
        return HPRecipes.PRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<PressRecipe> getType() {
        return HPRecipes.PRESSING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.PRESSING_CATEGORY.get();
    }

    public int getInputCount() {
        return inputCount;
    }

    /** Creates a FluidStack at runtime. Returns empty if no fluid output. */
    public FluidStack getFluidResult() {
        return fluidRef.map(FluidRef::create).orElse(FluidStack.EMPTY);
    }

    public boolean hasFluidOutput() {
        return fluidRef.isPresent();
    }

    // Codecs - use ItemStackTemplate.CODEC and deferred FluidRef to avoid bound-component issues
    public static final MapCodec<PressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(PressRecipe::getIngredient),
                    Codec.INT.optionalFieldOf("inputCount", 1).forGetter(PressRecipe::getInputCount),
                    ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(r -> Optional.ofNullable(r.getResult())),
                    FluidRef.CODEC.optionalFieldOf("fluidResult").forGetter(r -> r.fluidRef)
            ).apply(instance, PressRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PressRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, PressRecipe::getIngredient,
            ByteBufCodecs.VAR_INT, PressRecipe::getInputCount,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), r -> Optional.ofNullable(r.getResult()),
            ByteBufCodecs.optional(FluidRef.STREAM_CODEC), r -> r.fluidRef,
            PressRecipe::new
    );
}
