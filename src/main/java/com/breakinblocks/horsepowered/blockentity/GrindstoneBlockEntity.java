package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class GrindstoneBlockEntity extends HPBlockEntityHorseBase {

    private int currentItemMillTime;
    private int totalItemMillTime;

    public GrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDSTONE.get(), pos, state, 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("millTime", currentItemMillTime);
        output.putInt("totalMillTime", totalItemMillTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        if (!getItem(0).isEmpty()) {
            currentItemMillTime = input.getIntOr("millTime", 0);
            totalItemMillTime = input.getIntOr("totalMillTime", 1);
        } else {
            currentItemMillTime = 0;
            totalItemMillTime = 1;
        }
    }

    @Override
    public void setChanged() {
        if (getItem(0).isEmpty()) {
            currentItemMillTime = 0;
        }
        super.setChanged();
    }

    @Override
    public boolean targetReached() {
        currentItemMillTime++;

        if (currentItemMillTime >= totalItemMillTime) {
            currentItemMillTime = 0;
            totalItemMillTime = getRecipeTime();
            millItem();
            return true;
        }
        setChanged(); // Sync progress to client for Jade display
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.value().getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getRecipeSecondary() {
        return getRecipe().map(r -> r.value().getSecondary().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeSecondaryChance() {
        return getRecipe().map(r -> r.value().getSecondaryChance()).orElse(0);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(r -> r.value().getTime()).orElse(0);
    }

    public Optional<RecipeHolder<GrindstoneRecipe>> getRecipe() {
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        HPRecipeInput recipeInput = new HPRecipeInput(getItem(0));
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), recipeInput, serverLevel);
    }

    @Override
    public int getPositionOffset() {
        return 0;
    }

    private void millItem() {
        if (canWork()) {
            Optional<RecipeHolder<GrindstoneRecipe>> recipeOpt = getRecipe();
            if (recipeOpt.isEmpty()) return;

            GrindstoneRecipe recipe = recipeOpt.get().value();

            mergeOutput(1, recipe.getResult());
            processSecondary(recipe.getSecondary(), recipe.getSecondaryChance());

            getItem(0).shrink(1);
            setChanged();
        }
    }

    @Override
    protected void onInputChanged() {
        totalItemMillTime = getRecipeTime();
        currentItemMillTime = 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        if (!(level instanceof ServerLevel serverLevel)) return false;

        HPRecipeInput recipeInput = new HPRecipeInput(stack);
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), recipeInput, serverLevel)
                .isPresent();
    }

    @Override
    public int getOutputSlot() {
        return 2;
    }

    public int getCurrentMillTime() {
        return currentItemMillTime;
    }

    public int getTotalMillTime() {
        return totalItemMillTime;
    }
}
