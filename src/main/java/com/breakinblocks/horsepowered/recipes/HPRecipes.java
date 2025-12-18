package com.breakinblocks.horsepowered.recipes;

import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HPRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Reference.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MODID);

    // Recipe Types
    public static final RegistryObject<RecipeType<GrindstoneRecipe>> GRINDING_TYPE =
            RECIPE_TYPES.register("grinding", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Reference.MODID + ":grinding";
                }
            });

    public static final RegistryObject<RecipeType<ChoppingRecipe>> CHOPPING_TYPE =
            RECIPE_TYPES.register("chopping", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Reference.MODID + ":chopping";
                }
            });

    public static final RegistryObject<RecipeType<PressRecipe>> PRESSING_TYPE =
            RECIPE_TYPES.register("pressing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Reference.MODID + ":pressing";
                }
            });

    // Recipe Serializers
    public static final RegistryObject<RecipeSerializer<GrindstoneRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", GrindstoneRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ChoppingRecipe>> CHOPPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("chopping", ChoppingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<PressRecipe>> PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pressing", PressRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
