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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;

public class BottlingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient container;
    private final FluidStack fluid;
    private final ItemStack result;
    private final int priority;

    public BottlingRecipe(ResourceLocation id, Ingredient container, FluidStack fluid,
                          ItemStack result, int priority) {
        this.id = id;
        this.container = container;
        this.fluid = fluid;
        this.result = result;
        this.priority = priority;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return container.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
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
        list.add(container);
        return list;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HPRecipes.BOTTLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HPRecipes.BOTTLING_TYPE.get();
    }

    public Ingredient getContainer() {
        return container;
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isValid() {
        return !fluid.isEmpty() && !result.isEmpty() && container.getItems().length > 0;
    }

    public boolean matchesContainer(ItemStack stack) {
        return container.test(stack);
    }

    public boolean matchesResult(ItemStack stack) {
        return ItemStack.isSameItemSameTags(stack, result);
    }

    public ItemStack getEmptyContainer() {
        ItemStack[] items = container.getItems();
        if (items.length == 0) return ItemStack.EMPTY;
        ItemStack copy = items[0].copy();
        copy.setCount(1);
        return copy;
    }

    public static class Serializer implements RecipeSerializer<BottlingRecipe> {

        @Override
        public BottlingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient container = Ingredient.fromJson(json.get("container"));

            JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "fluid");
            ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(fluidJson, "id"));
            Fluid fluidType = BuiltInRegistries.FLUID.get(fluidId);
            int amount = GsonHelper.getAsInt(fluidJson, "amount", 250);
            FluidStack fluid = BuiltInRegistries.FLUID.containsKey(fluidId)
                    ? new FluidStack(fluidType, amount)
                    : FluidStack.EMPTY;

            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            int priority = GsonHelper.getAsInt(json, "priority", 0);
            return new BottlingRecipe(recipeId, container, fluid, result, priority);
        }

        @Override
        public BottlingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient container = Ingredient.fromNetwork(buffer);
            FluidStack fluid = buffer.readFluidStack();
            ItemStack result = buffer.readItem();
            int priority = buffer.readVarInt();
            return new BottlingRecipe(recipeId, container, fluid, result, priority);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BottlingRecipe recipe) {
            recipe.container.toNetwork(buffer);
            buffer.writeFluidStack(recipe.fluid);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.priority);
        }
    }
}
