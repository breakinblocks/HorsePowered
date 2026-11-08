package com.breakinblocks.horsepowered.util;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public final class WorkerStats {

    public static final DataMapType<EntityType<?>, Double> JUMP_STRENGTH = DataMapType.builder(
                    HorsePowerMod.id("horse_engine_jump_strength"), Registries.ENTITY_TYPE, Codec.DOUBLE)
            .build();

    private static final double MIN_SPEED_MULTIPLIER = 0.1;

    private WorkerStats() {}

    public static double resolveJumpStrength(PathfinderMob mob) {
        ResourceKey<EntityType<?>> key =
                BuiltInRegistries.ENTITY_TYPE.getResourceKey(mob.getType()).orElse(null);
        if (key != null) {
            Double override = BuiltInRegistries.ENTITY_TYPE.getData(JUMP_STRENGTH, key);
            if (override != null) {
                return override;
            }
        }
        if (mob.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
            return mob.getAttributeValue(Attributes.JUMP_STRENGTH);
        }
        return HorsePowerConfig.horseEngineDefaultJumpStrength.get();
    }

    public static double speedMultiplier(double jumpStrength) {
        double multiplier = 1.0
                + (jumpStrength - HorsePowerConfig.workerJumpSpeedBaseline.get())
                        * HorsePowerConfig.workerJumpSpeedScale.get();
        return Math.max(MIN_SPEED_MULTIPLIER, multiplier);
    }

    public static double engineStress(double jumpStrength) {
        return jumpStrength * 10.0 * HorsePowerConfig.horseEngineStressPerJumpPoint.get();
    }
}
