package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.Optional;

public class PressBlockEntity extends HPBlockEntityHorseBase {

    private final FluidTank inputTank;
    private final FluidTank outputTank;
    private final DualTankFluidHandler fluidHandler;
    private int currentPressStatus;

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PRESS_BE.get(), pos, state, 2);
        int capacity = HorsePowerConfig.pressFluidTankSize.get();
        this.inputTank = new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
        this.outputTank = new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
        this.fluidHandler = new DualTankFluidHandler(inputTank, outputTank);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("currentPressStatus", currentPressStatus);
        tag.put("fluidInput", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("fluidOutput", outputTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("fluidInput")) {
            inputTank.readFromNBT(registries, tag.getCompound("fluidInput"));
        } else {
            inputTank.setFluid(FluidStack.EMPTY);
        }
        if (tag.contains("fluidOutput")) {
            outputTank.readFromNBT(registries, tag.getCompound("fluidOutput"));
        } else {
            outputTank.setFluid(FluidStack.EMPTY);
        }

        // Legacy migration: pre-split saves used a single "fluid" key. Route it to the
        // output tank if the fluid matches any recipe result (likely mid-extraction),
        // otherwise treat it as pending input.
        if (inputTank.isEmpty() && outputTank.isEmpty() && tag.contains("fluid")) {
            FluidTank tmp = new FluidTank(inputTank.getCapacity());
            tmp.readFromNBT(registries, tag.getCompound("fluid"));
            FluidStack legacy = tmp.getFluid();
            if (!legacy.isEmpty()) {
                if (matchesAnyRecipeOutput(legacy)) {
                    outputTank.setFluid(legacy);
                } else {
                    inputTank.setFluid(legacy);
                }
            }
        }

        if (!getItem(0).isEmpty()) {
            currentPressStatus = tag.getInt("currentPressStatus");
        } else {
            currentPressStatus = 0;
        }
    }

    private boolean matchesAnyRecipeOutput(FluidStack fluid) {
        if (level == null || fluid.isEmpty()) return false;
        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .anyMatch(r -> {
                    PressRecipe rec = r.value();
                    return rec.hasFluidOutput()
                            && FluidStack.isSameFluidSameComponents(rec.getFluidResult(), fluid);
                });
    }

    @Override
    public boolean targetReached() {
        currentPressStatus++;

        int totalPress = HorsePowerConfig.pointsForPress.get();
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
        return getRecipe().map(r -> r.value().getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeInputCount() {
        return getRecipe().map(r -> r.value().getInputCount()).orElse(1);
    }

    public Optional<RecipeHolder<PressRecipe>> getRecipe() {
        if (level == null) return Optional.empty();
        HPRecipeInput input = new HPRecipeInput(getItem(0));
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.PRESSING_TYPE.get(), input, level);
    }

    @Override
    public int getPositionOffset() {
        return 0;
    }

    private void pressItem() {
        if (canWork()) {
            Optional<RecipeHolder<PressRecipe>> recipeOpt = getRecipe();
            if (recipeOpt.isEmpty()) return;

            PressRecipe recipe = recipeOpt.get().value();

            if (recipe.hasFluidOutput()) {
                outputTank.fill(recipe.getFluidResult().copy(), IFluidHandler.FluidAction.EXECUTE);
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

        Optional<RecipeHolder<PressRecipe>> recipeOpt = getRecipe();
        if (recipeOpt.isEmpty()) return false;

        PressRecipe recipe = recipeOpt.get().value();
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
            // Fluid output: item slot must be empty, output tank must have room for the result.
            return output.isEmpty()
                    && (outputTank.getFluidAmount() == 0
                        || outputTank.fill(fluidOutput.copy(), IFluidHandler.FluidAction.SIMULATE) >= fluidOutput.getAmount());
        } else {
            // Item output: output tank must be empty, output slot must have room.
            return outputTank.getFluidAmount() == 0
                    && (output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result)
                            && output.getCount() + result.getCount() <= output.getMaxStackSize()));
        }
    }

    @Override
    public int getInventoryStackLimit(ItemStack stack) {
        if (level == null) return getInventoryStackLimit();

        HPRecipeInput input = new HPRecipeInput(stack);
        Optional<RecipeHolder<PressRecipe>> recipeOpt = level.getRecipeManager()
                .getRecipeFor(HPRecipes.PRESSING_TYPE.get(), input, level);

        return recipeOpt.map(r -> r.value().getInputCount()).orElse(getInventoryStackLimit());
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return currentPressStatus == 0 ? super.removeItemNoUpdate(slot) : ItemStack.EMPTY;
    }

    @Override
    public int getInventoryStackLimit() {
        return getRecipe().map(r -> r.value().getInputCount()).orElse(64);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        if (currentPressStatus != 0) return false;
        if (level == null) return false;

        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .anyMatch(recipe -> recipe.value().getIngredient().test(stack));
    }

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    public DualTankFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public int getCurrentPressStatus() {
        return currentPressStatus;
    }

    public int getTotalPressPoints() {
        int total = HorsePowerConfig.pointsForPress.get();
        return total <= 0 ? 1 : total;
    }

    /**
     * Gets the visual press progress as a value from 0.0 (not pressed) to 1.0 (fully pressed).
     * Used by the renderer to animate the plunger.
     */
    public float getVisualProgress() {
        int total = getTotalPressPoints();
        if (total <= 0) return 0;
        return (float) currentPressStatus / total;
    }

    @Override
    public int getOutputSlot() {
        return 1;
    }
}
