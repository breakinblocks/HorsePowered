package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class GrindstoneBlockEntity extends HPBlockEntityHorseBase {

    private int currentItemMillTime;
    private int totalItemMillTime;

    public GrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GRINDSTONE_BE.get(), pos, state, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("millTime", currentItemMillTime);
        tag.putInt("totalMillTime", totalItemMillTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (!getItem(0).isEmpty()) {
            currentItemMillTime = tag.getInt("millTime");
            totalItemMillTime = tag.getInt("totalMillTime");
        } else {
            currentItemMillTime = 0;
            totalItemMillTime = 1;
        }
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
        if (level == null) return Optional.empty();
        HPRecipeInput input = new HPRecipeInput(getItem(0));
        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.GRINDING_TYPE.get()).stream()
                .filter(r -> r.value().getTier().allowsHorse())
                .filter(r -> r.value().matches(input, level))
                .findFirst();
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
        if (level == null) return false;

        HPRecipeInput input = new HPRecipeInput(stack);
        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.GRINDING_TYPE.get()).stream()
                .filter(r -> r.value().getTier().allowsHorse())
                .anyMatch(r -> r.value().matches(input, level));
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
