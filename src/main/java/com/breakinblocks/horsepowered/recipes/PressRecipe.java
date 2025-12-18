package com.breakinblocks.horsepowered.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PressRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final int inputCount;
    private final ItemStack result;
    private final FluidStack fluidResult;

    public PressRecipe(ResourceLocation id, Ingredient ingredient, int inputCount, ItemStack result, FluidStack fluidResult) {
        this.id = id;
        this.ingredient = ingredient;
        this.inputCount = inputCount;
        this.result = result;
        this.fluidResult = fluidResult;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack input = container.getItem(0);
        return ingredient.test(input) && input.getCount() >= inputCount;
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
        return HPRecipes.PRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.PRESSING_TYPE.get();
    }

    // Accessors
    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStack getResult() {
        return result;
    }

    public FluidStack getFluidResult() {
        return fluidResult;
    }

    public boolean hasFluidOutput() {
        return !fluidResult.isEmpty();
    }

    public static class Serializer implements RecipeSerializer<PressRecipe> {

        @Override
        public PressRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            int inputCount = GsonHelper.getAsInt(json, "inputCount", 1);
            ItemStack result = json.has("result")
                ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"))
                : ItemStack.EMPTY;
            FluidStack fluidResult = FluidStack.EMPTY;
            if (json.has("fluidResult")) {
                JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "fluidResult");
                String fluidName = GsonHelper.getAsString(fluidJson, "FluidName");
                int amount = GsonHelper.getAsInt(fluidJson, "Amount");
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
                if (fluid != null && fluid != Fluids.EMPTY) {
                    fluidResult = new FluidStack(fluid, amount);
                }
            }
            return new PressRecipe(recipeId, ingredient, inputCount, result, fluidResult);
        }

        @Override
        public PressRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            int inputCount = buffer.readInt();
            ItemStack result = buffer.readItem();
            FluidStack fluidResult = buffer.readFluidStack();
            return new PressRecipe(recipeId, ingredient, inputCount, result, fluidResult);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PressRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeInt(recipe.inputCount);
            buffer.writeItem(recipe.result);
            buffer.writeFluidStack(recipe.fluidResult);
        }
    }
}
