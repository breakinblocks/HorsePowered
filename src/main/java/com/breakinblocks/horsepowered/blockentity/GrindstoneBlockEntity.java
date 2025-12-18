package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class GrindstoneBlockEntity extends HPBlockEntityHorseBase {

    private int currentItemMillTime;
    private int totalItemMillTime;

    // Client-side rendering
    public ItemStack renderStack = ItemStack.EMPTY;

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
    public boolean validateArea() {
        if (level == null) return false;

        if (searchPos == null) {
            searchPos = Lists.newArrayList();

            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    // Skip center area (3x3 around the grindstone)
                    if ((x <= 1 && x >= -1) && (z <= 1 && z >= -1)) {
                        continue;
                    }
                    // Check Y=0 (where horse walks) and Y=1 (horse head clearance)
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
            ItemStack result = recipe.getResult();
            ItemStack secondary = recipe.getSecondary();

            ItemStack input = getItem(0);
            ItemStack output = getItem(1);
            ItemStack secondaryOutput = getItem(2);

            // Process main output
            if (output.isEmpty()) {
                setItem(1, result.copy());
            } else if (ItemStack.isSameItemSameTags(output, result)) {
                output.grow(result.getCount());
            }

            // Process secondary output
            processSecondaries(secondary, secondaryOutput, recipe.getSecondaryChance());

            input.shrink(1);
            setChanged();
        }
    }

    private void processSecondaries(ItemStack secondary, ItemStack secondaryOutput, int chance) {
        if (!secondary.isEmpty() && level != null) {
            if (chance >= 100 || level.random.nextInt(100) < chance) {
                if (secondaryOutput.isEmpty()) {
                    setItem(2, secondary.copy());
                } else if (ItemStack.isSameItemSameTags(secondaryOutput, secondary)) {
                    secondaryOutput.grow(secondary.getCount());
                }
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack oldStack = getItem(slot);
        super.setItem(slot, stack);

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, oldStack);
        if (slot == 0 && !isSameItem) {
            totalItemMillTime = getRecipeTime();
            currentItemMillTime = 0;
        }
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
