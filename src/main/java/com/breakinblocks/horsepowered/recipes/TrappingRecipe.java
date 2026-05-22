package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class TrappingRecipe extends BaseHPRecipe {

    private final Identifier entityId;
    private final int time;
    private final int priority;
    private final Optional<TagKey<Biome>> biome;
    private final boolean waterlogged;
    private final boolean baitConsumed;
    private final double baitConsumeChance;

    public TrappingRecipe(Ingredient bait, Identifier entityId, int time, int priority,
                          Optional<TagKey<Biome>> biome, boolean waterlogged,
                          boolean baitConsumed, double baitConsumeChance) {
        super(bait, null);
        this.entityId = entityId;
        this.time = Math.max(1, time);
        this.priority = priority;
        this.biome = biome;
        this.waterlogged = waterlogged;
        this.baitConsumed = baitConsumed;
        this.baitConsumeChance = Math.max(0.01D, Math.min(100.0D, baitConsumeChance));
    }

    @Override
    public RecipeSerializer<TrappingRecipe> getSerializer() {
        return HPRecipes.TRAPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<TrappingRecipe> getType() {
        return HPRecipes.TRAPPING_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return HPRecipes.TRAPPING_CATEGORY.get();
    }

    public Ingredient getBait() {
        return ingredient;
    }

    public Identifier getEntityId() {
        return entityId;
    }

    public int getTime() {
        return time;
    }

    public int getPriority() {
        return priority;
    }

    public Optional<TagKey<Biome>> getBiome() {
        return biome;
    }

    public boolean isWaterlogged() {
        return waterlogged;
    }

    public boolean isBaitConsumed() {
        return baitConsumed;
    }

    public double getBaitConsumeChance() {
        return baitConsumeChance;
    }

    public static final MapCodec<TrappingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("bait").forGetter(TrappingRecipe::getBait),
                    Identifier.CODEC.fieldOf("entity").forGetter(TrappingRecipe::getEntityId),
                    Codec.INT.optionalFieldOf("time", 1200).forGetter(TrappingRecipe::getTime),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(TrappingRecipe::getPriority),
                    TagKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(TrappingRecipe::getBiome),
                    Codec.BOOL.optionalFieldOf("waterlogged", false).forGetter(TrappingRecipe::isWaterlogged),
                    Codec.BOOL.optionalFieldOf("baitConsumed", false).forGetter(TrappingRecipe::isBaitConsumed),
                    Codec.doubleRange(0.01D, 100.0D).optionalFieldOf("baitConsumeChance", 100.0D).forGetter(TrappingRecipe::getBaitConsumeChance)
            ).apply(instance, TrappingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TrappingRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TrappingRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient bait = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Identifier entityId = Identifier.STREAM_CODEC.decode(buf);
            int time = ByteBufCodecs.VAR_INT.decode(buf);
            int priority = ByteBufCodecs.VAR_INT.decode(buf);
            Optional<TagKey<Biome>> biome = buf.readBoolean()
                    ? Optional.of(TagKey.create(Registries.BIOME, Identifier.STREAM_CODEC.decode(buf)))
                    : Optional.empty();
            boolean waterlogged = ByteBufCodecs.BOOL.decode(buf);
            boolean baitConsumed = ByteBufCodecs.BOOL.decode(buf);
            double baitConsumeChance = ByteBufCodecs.DOUBLE.decode(buf);
            return new TrappingRecipe(bait, entityId, time, priority, biome, waterlogged, baitConsumed, baitConsumeChance);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, TrappingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
            Identifier.STREAM_CODEC.encode(buf, recipe.entityId);
            ByteBufCodecs.VAR_INT.encode(buf, recipe.time);
            ByteBufCodecs.VAR_INT.encode(buf, recipe.priority);
            buf.writeBoolean(recipe.biome.isPresent());
            recipe.biome.ifPresent(tag -> Identifier.STREAM_CODEC.encode(buf, tag.location()));
            ByteBufCodecs.BOOL.encode(buf, recipe.waterlogged);
            ByteBufCodecs.BOOL.encode(buf, recipe.baitConsumed);
            ByteBufCodecs.DOUBLE.encode(buf, recipe.baitConsumeChance);
        }
    };
}
