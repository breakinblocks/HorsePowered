package com.breakinblocks.horsepowered.fluids;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, HorsePowerMod.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, HorsePowerMod.MOD_ID);

    // Seed oil - viscous plant-based oil
    public static final DeferredHolder<FluidType, FluidType> SEED_OIL_TYPE = FLUID_TYPES.register("seed_oil",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type." + HorsePowerMod.MOD_ID + ".seed_oil")
                    .density(920)       // Slightly less dense than water
                    .viscosity(3000)    // 3x more viscous than water — flows slow
                    .temperature(300)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            ));

    // Fluids must be declared before the properties supplier can reference them,
    // so we use a method to build properties lazily
    public static final DeferredHolder<Fluid, FlowingFluid> SEED_OIL_SOURCE = FLUIDS.register("seed_oil",
            () -> new BaseFlowingFluid.Source(seedOilProperties()));

    public static final DeferredHolder<Fluid, FlowingFluid> SEED_OIL_FLOWING = FLUIDS.register("flowing_seed_oil",
            () -> new BaseFlowingFluid.Flowing(seedOilProperties()));

    private static BaseFlowingFluid.Properties seedOilProperties() {
        return new BaseFlowingFluid.Properties(SEED_OIL_TYPE, SEED_OIL_SOURCE, SEED_OIL_FLOWING)
                .bucket(ModItems.SEED_OIL_BUCKET)
                .block(ModBlocks.SEED_OIL_BLOCK)
                .slopeFindDistance(3)      // Shorter search range than water (4)
                .levelDecreasePerBlock(2)  // Loses 2 levels per block — flows half as far as water
                .tickRate(15);             // 3x slower tick rate than water (5)
    }
}
