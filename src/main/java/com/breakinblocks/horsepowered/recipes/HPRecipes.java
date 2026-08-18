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

    public static final RegistryObject<RecipeType<DryingRackRecipe>> DRYING_TYPE =
            RECIPE_TYPES.register("drying", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Reference.MODID + ":drying";
                }
            });

    public static final RegistryObject<RecipeType<CrushingRecipe>> CRUSHING_TYPE =
            RECIPE_TYPES.register("crushing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Reference.MODID + ":crushing";
                }
            });

    public static final RegistryObject<RecipeType<TrappingRecipe>> TRAPPING_TYPE =
            RECIPE_TYPES.register("trapping", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Reference.MODID + ":trapping";
                }
            });

    public static final RegistryObject<RecipeType<BottlingRecipe>> BOTTLING_TYPE =
            RECIPE_TYPES.register("bottling", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Reference.MODID + ":bottling";
                }
            });

    // Recipe Serializers
    public static final RegistryObject<RecipeSerializer<GrindstoneRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", GrindstoneRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ChoppingRecipe>> CHOPPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("chopping", ChoppingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<PressRecipe>> PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pressing", PressRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<DryingRackRecipe>> DRYING_SERIALIZER =
            RECIPE_SERIALIZERS.register("drying", DryingRackRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<CrushingRecipe>> CRUSHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("crushing", CrushingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<TrappingRecipe>> TRAPPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("trapping", TrappingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<BottlingRecipe>> BOTTLING_SERIALIZER =
            RECIPE_SERIALIZERS.register("bottling", BottlingRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
