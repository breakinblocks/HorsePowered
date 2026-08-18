package com.breakinblocks.horsepowered.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.Optional;

public class TrappingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
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

    public TrappingRecipe(ResourceLocation id, Ingredient bait, ResourceLocation entityId,
                          int time, int priority, Optional<TagKey<Biome>> biome, boolean waterlogged,
                          boolean baitConsumed, double baitConsumeChance,
                          Optional<String> title, Optional<ResourceLocation> icon) {
        this.id = id;
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
    public boolean matches(Container container, Level level) {
        return bait.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(bait);
        return list;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HPRecipes.TRAPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.TRAPPING_TYPE.get();
    }

    public Ingredient getBait() { return bait; }
    public ResourceLocation getEntityId() { return entityId; }
    public int getTime() { return time; }
    public int getPriority() { return priority; }
    public Optional<TagKey<Biome>> getBiome() { return biome; }
    public boolean isWaterlogged() { return waterlogged; }
    public boolean isBaitConsumed() { return baitConsumed; }
    public double getBaitConsumeChance() { return baitConsumeChance; }
    public Optional<String> getTitle() { return title; }
    public Optional<ResourceLocation> getIcon() { return icon; }

    public ItemStack getDisplayIcon() {
        if (icon.isPresent() && BuiltInRegistries.ITEM.containsKey(icon.get())) {
            return new ItemStack(BuiltInRegistries.ITEM.get(icon.get()));
        }
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            SpawnEggItem egg = ForgeSpawnEggItem.fromEntityType(type);
            if (egg != null) return new ItemStack(egg);
        }
        ResourceLocation conventional = new ResourceLocation(
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

        @Override
        public TrappingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient bait = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "bait"));
            ResourceLocation entityId = new ResourceLocation(GsonHelper.getAsString(json, "entity"));
            int time = GsonHelper.getAsInt(json, "time", 1200);
            int priority = GsonHelper.getAsInt(json, "priority", 0);
            Optional<TagKey<Biome>> biome = Optional.empty();
            if (json.has("biome")) {
                String biomeStr = GsonHelper.getAsString(json, "biome");
                if (biomeStr.startsWith("#")) biomeStr = biomeStr.substring(1);
                biome = Optional.of(TagKey.create(Registries.BIOME, new ResourceLocation(biomeStr)));
            }
            boolean waterlogged = GsonHelper.getAsBoolean(json, "waterlogged", false);
            boolean baitConsumed = GsonHelper.getAsBoolean(json, "baitConsumed", false);
            double baitConsumeChance = GsonHelper.getAsDouble(json, "baitConsumeChance", 100.0D);
            Optional<String> title = json.has("title")
                    ? Optional.of(GsonHelper.getAsString(json, "title"))
                    : Optional.empty();
            Optional<ResourceLocation> icon = json.has("icon")
                    ? Optional.of(new ResourceLocation(GsonHelper.getAsString(json, "icon")))
                    : Optional.empty();
            return new TrappingRecipe(recipeId, bait, entityId, time, priority, biome, waterlogged,
                    baitConsumed, baitConsumeChance, title, icon);
        }

        @Override
        public TrappingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient bait = Ingredient.fromNetwork(buffer);
            ResourceLocation entityId = buffer.readResourceLocation();
            int time = buffer.readVarInt();
            int priority = buffer.readVarInt();
            Optional<TagKey<Biome>> biome = buffer.readBoolean()
                    ? Optional.of(TagKey.create(Registries.BIOME, buffer.readResourceLocation()))
                    : Optional.empty();
            boolean waterlogged = buffer.readBoolean();
            boolean baitConsumed = buffer.readBoolean();
            double baitConsumeChance = buffer.readDouble();
            Optional<String> title = buffer.readBoolean() ? Optional.of(buffer.readUtf()) : Optional.empty();
            Optional<ResourceLocation> icon = buffer.readBoolean()
                    ? Optional.of(buffer.readResourceLocation())
                    : Optional.empty();
            return new TrappingRecipe(recipeId, bait, entityId, time, priority, biome, waterlogged,
                    baitConsumed, baitConsumeChance, title, icon);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TrappingRecipe recipe) {
            recipe.bait.toNetwork(buffer);
            buffer.writeResourceLocation(recipe.entityId);
            buffer.writeVarInt(recipe.time);
            buffer.writeVarInt(recipe.priority);
            buffer.writeBoolean(recipe.biome.isPresent());
            recipe.biome.ifPresent(tag -> buffer.writeResourceLocation(tag.location()));
            buffer.writeBoolean(recipe.waterlogged);
            buffer.writeBoolean(recipe.baitConsumed);
            buffer.writeDouble(recipe.baitConsumeChance);
            buffer.writeBoolean(recipe.title.isPresent());
            recipe.title.ifPresent(buffer::writeUtf);
            buffer.writeBoolean(recipe.icon.isPresent());
            recipe.icon.ifPresent(buffer::writeResourceLocation);
        }
    }
}
