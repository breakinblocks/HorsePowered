package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("currentWindup", currentWindup);
        tag.putInt("chopTime", currentItemChopTime);
        tag.putInt("totalChopTime", totalItemChopTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
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

        SimpleContainer container = new SimpleContainer(stack);
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.CHOPPING_TYPE.get(), container, level)
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

        // Update visual windup
        float windup = Configs.pointsForWindup.get() > 0 ? Configs.pointsForWindup.get() : 1;
        visualWindup = -0.74F + (0.74F * (((float) currentWindup) / (windup - 1)));
    }

    @Override
    public boolean targetReached() {
        currentWindup++;

        if (currentWindup >= Configs.pointsForWindup.get()) {
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

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, oldStack);
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
            } else if (ItemStack.isSameItemSameTags(output, result)) {
                output.grow(result.getCount());
            }

            input.shrink(1);
            setChanged();
        }
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(ChoppingRecipe::getTime).orElse(1);
    }

    public Optional<ChoppingRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        SimpleContainer container = new SimpleContainer(getItem(0));
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.CHOPPING_TYPE.get(), container, level);
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
