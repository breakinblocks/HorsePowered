package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PressBlockEntity extends HPBlockEntityHorseBase {

    private final FluidTank inputTank;
    private final FluidTank outputTank;
    private final DualTankFluidHandler fluidHandler;
    private LazyOptional<IFluidHandler> fluidCap;
    private int currentPressStatus;

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PRESS_BE.get(), pos, state, 2);
        int capacity = Configs.pressFluidTankSize.get();
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
        this.fluidCap = LazyOptional.of(() -> fluidHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("currentPressStatus", currentPressStatus);
        tag.put("fluidInput", inputTank.writeToNBT(new CompoundTag()));
        tag.put("fluidOutput", outputTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("fluidInput")) {
            inputTank.readFromNBT(tag.getCompound("fluidInput"));
        } else {
            inputTank.setFluid(FluidStack.EMPTY);
        }
        if (tag.contains("fluidOutput")) {
            outputTank.readFromNBT(tag.getCompound("fluidOutput"));
        } else {
            outputTank.setFluid(FluidStack.EMPTY);
        }

        // Legacy migration: pre-split saves used a single "fluid" key. Route it to the
        // output tank if the fluid matches any recipe result (likely mid-extraction),
        // otherwise treat it as pending input.
        if (inputTank.isEmpty() && outputTank.isEmpty() && tag.contains("fluid")) {
            FluidTank tmp = new FluidTank(inputTank.getCapacity());
            tmp.readFromNBT(tag.getCompound("fluid"));
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
                .anyMatch(rec -> rec.hasFluidOutput()
                        && rec.getFluidResult().isFluidEqual(fluid));
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
            // Fluid output: item slot must be empty, output tank must have room.
            return output.isEmpty() &&
                    (outputTank.getFluidAmount() == 0
                        || outputTank.fill(fluidOutput.copy(), IFluidHandler.FluidAction.SIMULATE) >= fluidOutput.getAmount());
        } else {
            // Item output: output tank must be empty, output slot must have room.
            return outputTank.getFluidAmount() == 0 &&
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
    public int getInventoryStackLimit() {
        return getRecipe().map(PressRecipe::getInputCount).orElse(64);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        if (currentPressStatus != 0 && !getItem(0).isEmpty()) return false;
        if (level == null) return false;

        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .anyMatch(recipe -> recipe.getIngredient().test(stack));
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

    @Override
    public int getOutputSlot() {
        return 1;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidCap = LazyOptional.of(() -> fluidHandler);
    }
}
