package com.breakinblocks.horsepowered.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Base class for all HorsePowered recipes, providing shared field storage and method implementations.
 * Results are stored as {@link ItemStackTemplate} to avoid bound-component issues during recipe loading.
 */
public abstract class BaseHPRecipe implements Recipe<HPRecipeInput> {

    protected final Ingredient ingredient;
    protected final @Nullable ItemStackTemplate result;

    protected BaseHPRecipe(Ingredient ingredient, @Nullable ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(HPRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(HPRecipeInput input) {
        return createResult();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredient);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public @Nullable ItemStackTemplate getResult() {
        return result;
    }

    /**
     * Creates a new ItemStack from the result template.
     * Safe to call at runtime when item components are bound.
     */
    public ItemStack createResult() {
        return result != null ? result.create() : ItemStack.EMPTY;
    }
}
