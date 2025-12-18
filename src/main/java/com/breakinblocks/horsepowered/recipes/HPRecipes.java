package com.breakinblocks.horsepowered.recipes;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HPRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, HorsePowerMod.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, HorsePowerMod.MOD_ID);

    // Recipe Types
    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindstoneRecipe>> GRINDING_TYPE =
            RECIPE_TYPES.register("grinding", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return HorsePowerMod.MOD_ID + ":grinding";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<ChoppingRecipe>> CHOPPING_TYPE =
            RECIPE_TYPES.register("chopping", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return HorsePowerMod.MOD_ID + ":chopping";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<PressRecipe>> PRESSING_TYPE =
            RECIPE_TYPES.register("pressing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return HorsePowerMod.MOD_ID + ":pressing";
                }
            });

    // Recipe Serializers
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindstoneRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", GrindstoneRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ChoppingRecipe>> CHOPPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("chopping", ChoppingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PressRecipe>> PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pressing", PressRecipe.Serializer::new);
}
