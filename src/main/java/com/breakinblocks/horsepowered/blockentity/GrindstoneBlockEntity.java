package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class GrindstoneBlockEntity extends HPBlockEntityHorseBase {

    private int currentItemMillTime;
    private int totalItemMillTime;

    public GrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GRINDSTONE_BE.get(), pos, state, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("millTime", currentItemMillTime);
        tag.putInt("totalMillTime", totalItemMillTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (!getItem(0).isEmpty()) {
            currentItemMillTime = tag.getInt("millTime");
            totalItemMillTime = tag.getInt("totalMillTime");
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
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getRecipeSecondary() {
        return getRecipe().map(r -> r.getSecondary().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeSecondaryChance() {
        return getRecipe().map(GrindstoneRecipe::getSecondaryChance).orElse(0);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(GrindstoneRecipe::getTime).orElse(0);
    }

    public Optional<GrindstoneRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        SimpleContainer container = new SimpleContainer(getItem(0));
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), container, level);
    }

    @Override
    public int getPositionOffset() {
        return 0;
    }

    private void millItem() {
        if (canWork()) {
            Optional<GrindstoneRecipe> recipeOpt = getRecipe();
            if (recipeOpt.isEmpty()) return;

            GrindstoneRecipe recipe = recipeOpt.get();
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
        if (level == null) return false;

        SimpleContainer container = new SimpleContainer(stack);
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), container, level)
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
