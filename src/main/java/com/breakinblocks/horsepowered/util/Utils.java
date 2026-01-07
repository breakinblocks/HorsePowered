package com.breakinblocks.horsepowered.util;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Entity tag for valid worker mobs that can power horse-powered blocks.
     * Modpack makers can add entities to this tag via datapacks.
     */
    public static final TagKey<EntityType<?>> VALID_WORKER_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "valid_worker")
    );

    /**
     * Checks if an entity is a valid worker for horse-powered blocks
     */
    public static boolean isValidWorker(Entity entity) {
        return entity instanceof PathfinderMob && entity.getType().is(VALID_WORKER_TAG);
    }

    /**
     * Gets all valid creatures within a bounding box that can power horse-powered blocks.
     * Uses the horsepowered:valid_worker entity tag for filtering.
     */
    public static List<PathfinderMob> getValidCreatures(Level level, AABB searchArea) {
        return level.getEntitiesOfClass(PathfinderMob.class, searchArea)
                .stream()
                .filter(Utils::isValidWorker)
                .collect(Collectors.toList());
    }
}
