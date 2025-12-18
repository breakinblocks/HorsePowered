package com.breakinblocks.horsepowered.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class GrindstoneRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final ItemStack secondary;
    private final int secondaryChance;
    private final int time;

    public GrindstoneRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, ItemStack secondary, int secondaryChance, int time) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.secondary = secondary;
        this.secondaryChance = Math.max(0, Math.min(100, secondaryChance));
        this.time = time;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HPRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.GRINDING_TYPE.get();
    }

    // Accessors
    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public ItemStack getSecondary() {
        return secondary;
    }

    public int getSecondaryChance() {
        return secondaryChance;
    }

    public int getTime() {
        return time;
    }

    public static class Serializer implements RecipeSerializer<GrindstoneRecipe> {

        @Override
        public GrindstoneRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            ItemStack secondary = json.has("secondary")
                ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "secondary"))
                : ItemStack.EMPTY;
            int secondaryChance = GsonHelper.getAsInt(json, "secondaryChance", 0);
            int time = GsonHelper.getAsInt(json, "time");
            return new GrindstoneRecipe(recipeId, ingredient, result, secondary, secondaryChance, time);
        }

        @Override
        public GrindstoneRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            ItemStack secondary = buffer.readItem();
            int secondaryChance = buffer.readInt();
            int time = buffer.readInt();
            return new GrindstoneRecipe(recipeId, ingredient, result, secondary, secondaryChance, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GrindstoneRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeItem(recipe.secondary);
            buffer.writeInt(recipe.secondaryChance);
            buffer.writeInt(recipe.time);
        }
    }
}
