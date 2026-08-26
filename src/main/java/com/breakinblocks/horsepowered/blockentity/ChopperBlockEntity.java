package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class ChopperBlockEntity extends HPBlockEntityHorseBase {

    private int currentWindup;
    private int currentItemChopTime;
    private int totalItemChopTime;
    private float visualWindup = 0;

    public ChopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHOPPER.get(), pos, state, 2);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("currentWindup", currentWindup);
        output.putInt("chopTime", currentItemChopTime);
        output.putInt("totalChopTime", totalItemChopTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        currentWindup = input.getIntOr("currentWindup", 0);

        if (!getItem(0).isEmpty()) {
            currentItemChopTime = input.getIntOr("chopTime", 0);
            totalItemChopTime = input.getIntOr("totalChopTime", 1);
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
        // Only check input slot - output slot state shouldn't block insertion
        ItemStack inputSlot = getItem(0);
        if (!inputSlot.isEmpty()) {
            // Input slot has items - check if we can combine
            if (!ItemStack.isSameItemSameComponents(inputSlot, stack)) return false;
            if (inputSlot.getCount() >= getInventoryStackLimit()) return false;
        }
        return findRecipe(HPRecipes.CHOPPING_TYPE.get(), stack, r -> r.getTier().allowsHorse()).isPresent();
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
            ItemStack result = getRecipeOutput();
            mergeOutput(1, result);

            getItem(0).shrink(1);
            setChanged();
        }
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.value().createResult()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(r -> r.value().getTime()).orElse(1);
    }

    public Optional<RecipeHolder<ChoppingRecipe>> getRecipe() {
        return findRecipe(HPRecipes.CHOPPING_TYPE.get(), getItem(0), r -> r.getTier().allowsHorse());
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

    public int getCurrentChopTime() {
        return currentItemChopTime;
    }

    public int getTotalChopTime() {
        return totalItemChopTime;
    }

    public float getVisualWindup() {
        return visualWindup;
    }
}
