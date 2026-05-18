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

public class DryingRackRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int time;

    public DryingRackRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int time) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.time = Math.max(1, time);
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
        return HPRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.DRYING_TYPE.get();
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

    public static class Serializer implements RecipeSerializer<DryingRackRecipe> {

        @Override
        public DryingRackRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "time");
            return new DryingRackRecipe(recipeId, ingredient, result, time);
        }

        @Override
        public DryingRackRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int time = buffer.readInt();
            return new DryingRackRecipe(recipeId, ingredient, result, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DryingRackRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeInt(recipe.time);
        }
    }
}
