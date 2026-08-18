package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
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
    private final Optional<String> title;
    private final Optional<Identifier> icon;

    public TrappingRecipe(Ingredient bait, Identifier entityId, int time, int priority,
                          Optional<TagKey<Biome>> biome, boolean waterlogged,
                          boolean baitConsumed, double baitConsumeChance,
                          Optional<String> title, Optional<Identifier> icon) {
        super(bait, null);
        this.entityId = entityId;
        this.time = Math.max(1, time);
        this.priority = priority;
        this.biome = biome;
        this.waterlogged = waterlogged;
        this.baitConsumed = baitConsumed;
        this.baitConsumeChance = Math.max(0.01D, Math.min(100.0D, baitConsumeChance));
        this.title = title.filter(value -> !value.isBlank());
        this.icon = icon;
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

    public Optional<String> getTitle() {
        return title;
    }

    public Optional<Identifier> getIcon() {
        return icon;
    }

    public ItemStack getDisplayIcon() {
        if (icon.isPresent() && BuiltInRegistries.ITEM.containsKey(icon.get())) {
            return new ItemStack(BuiltInRegistries.ITEM.getValue(icon.get()));
        }
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
            Optional<Holder<Item>> egg = SpawnEggItem.byId(type);
            if (egg.isPresent()) return new ItemStack(egg.get().value());
        }
        Identifier conventional = Identifier.fromNamespaceAndPath(
                entityId.getNamespace(), entityId.getPath() + "_spawn_egg");
        if (BuiltInRegistries.ITEM.containsKey(conventional)) {
            return new ItemStack(BuiltInRegistries.ITEM.getValue(conventional));
        }
        return new ItemStack(Items.EGG);
    }

    public Component getDisplayName() {
        if (title.isPresent()) {
            String value = title.get();
            return Component.translatableWithFallback(value, value);
        }
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            return BuiltInRegistries.ENTITY_TYPE.getValue(entityId).getDescription();
        }
        return Component.literal(entityId.toString());
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
                    Codec.doubleRange(0.01D, 100.0D).optionalFieldOf("baitConsumeChance", 100.0D).forGetter(TrappingRecipe::getBaitConsumeChance),
                    Codec.STRING.optionalFieldOf("title").forGetter(TrappingRecipe::getTitle),
                    Identifier.CODEC.optionalFieldOf("icon").forGetter(TrappingRecipe::getIcon)
            ).apply(instance, TrappingRecipe::new)
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<String>> OPTIONAL_STRING =
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).cast();

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Identifier>> OPTIONAL_ID =
            ByteBufCodecs.optional(Identifier.STREAM_CODEC).cast();

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
            Optional<String> title = OPTIONAL_STRING.decode(buf);
            Optional<Identifier> icon = OPTIONAL_ID.decode(buf);
            return new TrappingRecipe(bait, entityId, time, priority, biome, waterlogged, baitConsumed, baitConsumeChance, title, icon);
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
            OPTIONAL_STRING.encode(buf, recipe.title);
            OPTIONAL_ID.encode(buf, recipe.icon);
        }
    };
}
