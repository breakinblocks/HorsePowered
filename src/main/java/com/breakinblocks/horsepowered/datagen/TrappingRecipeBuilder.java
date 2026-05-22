package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class TrappingRecipeBuilder {

    private final Ingredient bait;
    private final Identifier entityId;
    private int time = 1200;
    private int priority = 0;
    private Optional<TagKey<Biome>> biome = Optional.empty();
    private boolean waterlogged = false;
    private boolean baitConsumed = false;
    private double baitConsumeChance = 100.0D;

    private TrappingRecipeBuilder(Ingredient bait, Identifier entityId) {
        this.bait = bait;
        this.entityId = entityId;
    }

    public static TrappingRecipeBuilder trap(Ingredient bait, EntityType<?> entity) {
        return new TrappingRecipeBuilder(bait, BuiltInRegistries.ENTITY_TYPE.getKey(entity));
    }

    public TrappingRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public TrappingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public TrappingRecipeBuilder biome(TagKey<Biome> biome) {
        this.biome = Optional.of(biome);
        return this;
    }

    public TrappingRecipeBuilder waterlogged() {
        this.waterlogged = true;
        return this;
    }

    public TrappingRecipeBuilder baitConsumed(boolean consumed) {
        this.baitConsumed = consumed;
        return this;
    }

    public TrappingRecipeBuilder baitConsumeChance(double chance) {
        this.baitConsumeChance = chance;
        return this;
    }

    public void save(RecipeOutput output, String name) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                HorsePowerMod.id("trapping/" + name));
        output.accept(key, new TrappingRecipe(bait, entityId, time, priority, biome, waterlogged, baitConsumed, baitConsumeChance), null);
    }
}
