package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ChopperBlockEntity extends HPBlockEntityHorseBase {

    private int currentWindup;
    private int currentItemChopTime;
    private int totalItemChopTime;
    private float visualWindup = 0;

    public ChopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CHOPPER_BE.get(), pos, state, 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("currentWindup", currentWindup);
        tag.putInt("chopTime", currentItemChopTime);
        tag.putInt("totalChopTime", totalItemChopTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        currentWindup = tag.getInt("currentWindup");

        if (!getItem(0).isEmpty()) {
            currentItemChopTime = tag.getInt("chopTime");
            totalItemChopTime = tag.getInt("totalChopTime");
        } else {
            currentItemChopTime = 0;
            totalItemChopTime = 1;
        }
    }

    @Override
    public boolean canBeRotated() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        if (!getItem(1).isEmpty() || !getItem(0).isEmpty()) return false;
        if (level == null) return false;

        HPRecipeInput input = new HPRecipeInput(stack);
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.CHOPPING_TYPE.get(), input, level)
                .isPresent();
    }

    @Override
    protected void tickServer() {
        super.tickServer();
        updateVisualWindup();
    }

    @Override
    protected void tickClient() {
        super.tickClient();
        updateVisualWindup();
    }

    private void updateVisualWindup() {
        // Update visual windup based on current progress
        float windup = HorsePowerConfig.pointsForWindup.get() > 0 ? HorsePowerConfig.pointsForWindup.get() : 1;
        visualWindup = -0.74F + (0.74F * (((float) currentWindup) / (windup - 1)));
    }

    @Override
    public boolean targetReached() {
        currentWindup++;

        if (currentWindup >= HorsePowerConfig.pointsForWindup.get()) {
            currentWindup = 0;
            currentItemChopTime++;

            if (currentItemChopTime >= totalItemChopTime) {
                currentItemChopTime = 0;
                totalItemChopTime = getRecipeTime();
                chopItem();
                return true;
            }
        }
        setChanged();
        return false;
    }

    @Override
    protected void onInputChanged() {
        totalItemChopTime = getRecipeTime();
        currentItemChopTime = 0;
        currentWindup = 0;
    }

    private void chopItem() {
        if (canWork()) {
            mergeOutput(1, getRecipeOutput());
            getItem(0).shrink(1);
            setChanged();
        }
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.value().getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(r -> r.value().getTime()).orElse(1);
    }

    public Optional<RecipeHolder<ChoppingRecipe>> getRecipe() {
        if (level == null) return Optional.empty();
        HPRecipeInput input = new HPRecipeInput(getItem(0));
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.CHOPPING_TYPE.get(), input, level);
    }

    @Override
    public int getPositionOffset() {
        return 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public int getOutputSlot() {
        return 1;
    }

    /**
     * Returns the combined progress including the current windup sub-step.
     * This gives Jade a smooth 0-100% bar even when recipe time=1.
     */
    public int getCurrentChopTime() {
        int pointsPerCycle = HorsePowerConfig.pointsForWindup.get();
        return currentItemChopTime * pointsPerCycle + currentWindup;
    }

    public int getTotalChopTime() {
        int pointsPerCycle = HorsePowerConfig.pointsForWindup.get();
        return Math.max(1, totalItemChopTime) * pointsPerCycle;
    }

    public float getVisualWindup() {
        return visualWindup;
    }
}
