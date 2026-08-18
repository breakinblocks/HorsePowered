package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class TrappingRecipe implements Recipe<HPRecipeInput> {

    private final Ingredient bait;
    private final ResourceLocation entityId;
    private final int time;
    private final int priority;
    private final Optional<TagKey<Biome>> biome;
    private final boolean waterlogged;
    private final boolean baitConsumed;
    private final double baitConsumeChance;
    private final Optional<String> title;
    private final Optional<ResourceLocation> icon;

    public TrappingRecipe(Ingredient bait, ResourceLocation entityId, int time, int priority,
                          Optional<TagKey<Biome>> biome, boolean waterlogged,
                          boolean baitConsumed, double baitConsumeChance,
                          Optional<String> title, Optional<ResourceLocation> icon) {
        this.bait = bait;
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
    public boolean matches(HPRecipeInput input, Level level) {
        return bait.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(HPRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(bait);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HPRecipes.TRAPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.TRAPPING_TYPE.get();
    }

    public Ingredient getBait() {
        return bait;
    }

    public ResourceLocation getEntityId() {
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

    public Optional<ResourceLocation> getIcon() {
        return icon;
    }

    public ItemStack getDisplayIcon() {
        if (icon.isPresent() && BuiltInRegistries.ITEM.containsKey(icon.get())) {
            return new ItemStack(BuiltInRegistries.ITEM.get(icon.get()));
        }
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            SpawnEggItem egg = SpawnEggItem.byId(BuiltInRegistries.ENTITY_TYPE.get(entityId));
            if (egg != null) return new ItemStack(egg);
        }
        ResourceLocation conventional = ResourceLocation.fromNamespaceAndPath(
                entityId.getNamespace(), entityId.getPath() + "_spawn_egg");
        if (BuiltInRegistries.ITEM.containsKey(conventional)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(conventional));
        }
        return new ItemStack(Items.EGG);
    }

    public Component getDisplayName() {
        if (title.isPresent()) {
            String value = title.get();
            return Component.translatableWithFallback(value, value);
        }
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            return BuiltInRegistries.ENTITY_TYPE.get(entityId).getDescription();
        }
        return Component.literal(entityId.toString());
    }

    public static class Serializer implements RecipeSerializer<TrappingRecipe> {

        private static final StreamCodec<RegistryFriendlyByteBuf, Optional<String>> OPTIONAL_STRING =
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional).cast();

        private static final StreamCodec<RegistryFriendlyByteBuf, Optional<ResourceLocation>> OPTIONAL_ID =
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional).cast();

        public static final MapCodec<TrappingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("bait").forGetter(TrappingRecipe::getBait),
                        ResourceLocation.CODEC.fieldOf("entity").forGetter(TrappingRecipe::getEntityId),
                        Codec.INT.optionalFieldOf("time", 1200).forGetter(TrappingRecipe::getTime),
                        Codec.INT.optionalFieldOf("priority", 0).forGetter(TrappingRecipe::getPriority),
                        TagKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(TrappingRecipe::getBiome),
                        Codec.BOOL.optionalFieldOf("waterlogged", false).forGetter(TrappingRecipe::isWaterlogged),
                        Codec.BOOL.optionalFieldOf("baitConsumed", false).forGetter(TrappingRecipe::isBaitConsumed),
                        Codec.doubleRange(0.01D, 100.0D).optionalFieldOf("baitConsumeChance", 100.0D).forGetter(TrappingRecipe::getBaitConsumeChance),
                        Codec.STRING.optionalFieldOf("title").forGetter(TrappingRecipe::getTitle),
                        ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(TrappingRecipe::getIcon)
                ).apply(instance, TrappingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, TrappingRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public TrappingRecipe decode(RegistryFriendlyByteBuf buf) {
                Ingredient bait = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                ResourceLocation entityId = ResourceLocation.STREAM_CODEC.decode(buf);
                int time = ByteBufCodecs.VAR_INT.decode(buf);
                int priority = ByteBufCodecs.VAR_INT.decode(buf);
                Optional<TagKey<Biome>> biome = buf.readBoolean()
                        ? Optional.of(TagKey.create(Registries.BIOME, ResourceLocation.STREAM_CODEC.decode(buf)))
                        : Optional.empty();
                boolean waterlogged = ByteBufCodecs.BOOL.decode(buf);
                boolean baitConsumed = ByteBufCodecs.BOOL.decode(buf);
                double baitConsumeChance = ByteBufCodecs.DOUBLE.decode(buf);
                Optional<String> title = OPTIONAL_STRING.decode(buf);
                Optional<ResourceLocation> icon = OPTIONAL_ID.decode(buf);
                return new TrappingRecipe(bait, entityId, time, priority, biome, waterlogged, baitConsumed, baitConsumeChance, title, icon);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, TrappingRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.bait);
                ResourceLocation.STREAM_CODEC.encode(buf, recipe.entityId);
                ByteBufCodecs.VAR_INT.encode(buf, recipe.time);
                ByteBufCodecs.VAR_INT.encode(buf, recipe.priority);
                buf.writeBoolean(recipe.biome.isPresent());
                recipe.biome.ifPresent(tag -> ResourceLocation.STREAM_CODEC.encode(buf, tag.location()));
                ByteBufCodecs.BOOL.encode(buf, recipe.waterlogged);
                ByteBufCodecs.BOOL.encode(buf, recipe.baitConsumed);
                ByteBufCodecs.DOUBLE.encode(buf, recipe.baitConsumeChance);
                OPTIONAL_STRING.encode(buf, recipe.title);
                OPTIONAL_ID.encode(buf, recipe.icon);
            }
        };

        @Override
        public MapCodec<TrappingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TrappingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
