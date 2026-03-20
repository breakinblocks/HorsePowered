package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.Optional;

public class PressBlockEntity extends HPBlockEntityHorseBase {

    private final FluidTank tank;
    private int currentPressStatus;

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PRESS_BE.get(), pos, state, 2);
        this.tank = new FluidTank(Configs.pressFluidTankSize.get()) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("currentPressStatus", currentPressStatus);
        tag.put("fluid", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("fluid"));

        if (!getItem(0).isEmpty()) {
            currentPressStatus = tag.getInt("currentPressStatus");
        } else {
            currentPressStatus = 0;
        }
    }

    @Override
    public void setChanged() {
        if (getItem(0).isEmpty()) {
            currentPressStatus = 0;
        }
        super.setChanged();
    }

    @Override
    public boolean targetReached() {
        currentPressStatus++;

        int totalPress = Configs.pointsForPress.get();
        if (currentPressStatus >= (totalPress <= 0 ? 1 : totalPress)) {
            currentPressStatus = 0;
            pressItem();
            return true;
        }
        setChanged();
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeInputCount() {
        return getRecipe().map(PressRecipe::getInputCount).orElse(1);
    }

    public Optional<PressRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        SimpleContainer container = new SimpleContainer(getItem(0));
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.PRESSING_TYPE.get(), container, level);
    }

    @Override
    public int getPositionOffset() {
        return 0;
    }

    private void pressItem() {
        if (canWork()) {
            Optional<PressRecipe> recipeOpt = getRecipe();
            if (recipeOpt.isEmpty()) return;

            PressRecipe recipe = recipeOpt.get();

            if (recipe.hasFluidOutput()) {
                tank.fill(recipe.getFluidResult().copy(), IFluidHandler.FluidAction.EXECUTE);
            } else {
                mergeOutput(1, recipe.getResult());
            }

            getItem(0).shrink(recipe.getInputCount());
            setChanged();
        }
    }

    @Override
    protected void onInputChanged() {
        currentPressStatus = 0;
    }

    @Override
    public boolean canWork() {
        if (getItem(0).isEmpty()) {
            return false;
        }

        Optional<PressRecipe> recipeOpt = getRecipe();
        if (recipeOpt.isEmpty()) return false;

        PressRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.getResult();
        FluidStack fluidOutput = recipe.getFluidResult();

        if (getItem(0).getCount() < recipe.getInputCount()) {
            return false;
        }

        if (result.isEmpty() && !recipe.hasFluidOutput()) {
            return false;
        }

        ItemStack output = getItem(1);
        if (recipe.hasFluidOutput()) {
            // For fluid output, output slot must be empty and tank must have room
            return output.isEmpty() &&
                    (tank.getFluidAmount() == 0 || tank.fill(fluidOutput.copy(), IFluidHandler.FluidAction.SIMULATE) >= fluidOutput.getAmount());
        } else {
            // For item output, tank must be empty and output slot must have room
            return tank.getFluidAmount() == 0 &&
                    (output.isEmpty() || (ItemStack.isSameItemSameTags(output, result) &&
                            output.getCount() + result.getCount() <= output.getMaxStackSize()));
        }
    }

    @Override
    public int getInventoryStackLimit(ItemStack stack) {
        if (level == null) return getInventoryStackLimit();

        SimpleContainer container = new SimpleContainer(stack);
        Optional<PressRecipe> recipeOpt = level.getRecipeManager()
                .getRecipeFor(HPRecipes.PRESSING_TYPE.get(), container, level);

        return recipeOpt.map(PressRecipe::getInputCount).orElse(getInventoryStackLimit());
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return currentPressStatus == 0 ? super.removeItemNoUpdate(slot) : ItemStack.EMPTY;
    }

    @Override
    public int getInventoryStackLimit() {
        return getRecipe().map(PressRecipe::getInputCount).orElse(64);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        // Only reject if pressing is in progress (don't reject just because output has items)
        if (currentPressStatus != 0) return false;
        if (level == null) return false;

        // Check if ANY press recipe accepts this item type (ignore count requirement)
        // This allows hoppers to insert items one at a time
        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .anyMatch(recipe -> recipe.getIngredient().test(stack));
    }

    public FluidTank getTank() {
        return tank;
    }

    public int getCurrentPressStatus() {
        return currentPressStatus;
    }

    @Override
    public int getOutputSlot() {
        return 1;
    }
}
