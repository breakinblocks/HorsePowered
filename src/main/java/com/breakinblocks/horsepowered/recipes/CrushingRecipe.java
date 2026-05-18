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

public class CrushingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int time;
    private final int priority;
    private final float hungerCost;

    public CrushingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int time, int priority, float hungerCost) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.time = time;
        this.priority = priority;
        this.hungerCost = Math.max(0.0F, hungerCost);
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
        return HPRecipes.CRUSHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.CRUSHING_TYPE.get();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getTime() {
        return time;
    }

    public int getPriority() {
        return priority;
    }

    public float getHungerCost() {
        return hungerCost;
    }

    public static class Serializer implements RecipeSerializer<CrushingRecipe> {

        @Override
        public CrushingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "time");
            int priority = GsonHelper.getAsInt(json, "priority", 0);
            float hungerCost = GsonHelper.getAsFloat(json, "hungerCost", 0.0F);
            return new CrushingRecipe(recipeId, ingredient, result, time, priority, hungerCost);
        }

        @Override
        public CrushingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int time = buffer.readInt();
            int priority = buffer.readInt();
            float hungerCost = buffer.readFloat();
            return new CrushingRecipe(recipeId, ingredient, result, time, priority, hungerCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CrushingRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeInt(recipe.time);
            buffer.writeInt(recipe.priority);
            buffer.writeFloat(recipe.hungerCost);
        }
    }
}
