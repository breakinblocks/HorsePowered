package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;

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
        if (!getItem(1).isEmpty() || !getItem(0).isEmpty()) return false;
        if (!(level instanceof ServerLevel serverLevel)) return false;

        HPRecipeInput recipeInput = new HPRecipeInput(stack);
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.CHOPPING_TYPE.get(), recipeInput, serverLevel)
                .isPresent();
    }

    @Override
    public boolean validateArea() {
        if (level == null) return false;

        if (searchPos == null) {
            searchPos = Lists.newArrayList();

            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    // Skip center area
                    if ((x <= 1 && x >= -1) && (z <= 1 && z >= -1)) {
                        continue;
                    }
                    searchPos.add(worldPosition.offset(x, 0, z));
                    searchPos.add(worldPosition.offset(x, 1, z));
                }
            }
        }

        for (BlockPos pos : searchPos) {
            BlockState state = level.getBlockState(pos);
            if (!state.canBeReplaced()) {
                return false;
            }
        }
        return true;
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
    public void setItem(int slot, ItemStack stack) {
        ItemStack oldStack = getItem(slot);
        super.setItem(slot, stack);

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, oldStack);
        if (slot == 0 && !isSameItem) {
            totalItemChopTime = getRecipeTime();
            currentItemChopTime = 0;
            currentWindup = 0;
        }
    }

    private void chopItem() {
        if (canWork()) {
            ItemStack input = getItem(0);
            ItemStack result = getRecipeOutput();
            ItemStack output = getItem(1);

            if (output.isEmpty()) {
                setItem(1, result.copy());
            } else if (ItemStack.isSameItemSameComponents(output, result)) {
                output.grow(result.getCount());
            }

            input.shrink(1);
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
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        HPRecipeInput recipeInput = new HPRecipeInput(getItem(0));
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.CHOPPING_TYPE.get(), recipeInput, serverLevel);
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
