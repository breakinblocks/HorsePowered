package com.breakinblocks.horsepowered.util;

import com.breakinblocks.horsepowered.Configs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class WorkerStats {

    private static final double MIN_SPEED_MULTIPLIER = 0.1;

    private WorkerStats() {}

    public static double resolveJumpStrength(PathfinderMob mob) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        Double override = JumpStrengthOverrides.lookup(mob.getType(), id);
        if (override != null) {
            return override;
        }
        if (mob.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
            return mob.getAttributeValue(Attributes.JUMP_STRENGTH);
        }
        return Configs.horseEngineDefaultJumpStrength.get();
    }

    public static double speedMultiplier(double jumpStrength) {
        double multiplier =
                1.0 + (jumpStrength - Configs.workerJumpSpeedBaseline.get()) * Configs.workerJumpSpeedScale.get();
        return Math.max(MIN_SPEED_MULTIPLIER, multiplier);
    }

    public static double engineStress(double jumpStrength) {
        return jumpStrength * 10.0 * Configs.horseEngineStressPerJumpPoint.get();
    }
}
