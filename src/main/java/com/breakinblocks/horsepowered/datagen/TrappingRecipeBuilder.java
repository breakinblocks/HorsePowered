package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class TrappingRecipeBuilder {

    private final Ingredient bait;
    private final ResourceLocation entityId;
    private int time = 1200;
    private int priority = 0;
    private Optional<TagKey<Biome>> biome = Optional.empty();
    private boolean waterlogged = false;

    private TrappingRecipeBuilder(Ingredient bait, ResourceLocation entityId) {
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

    public void save(RecipeOutput output, String name) {
        ResourceLocation id = HorsePowerMod.id("trapping/" + name);
        output.accept(id, new TrappingRecipe(bait, entityId, time, priority, biome, waterlogged), null);
    }
}
