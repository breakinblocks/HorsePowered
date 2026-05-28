package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

public class PressBlockEntity extends HPBlockEntityHorseBase {

    private static final String TANKS_KEY = "tanks";

    private final PressTanks tanks;
    private final DualTankFluidHandler fluidHandler;
    private int currentPressStatus;

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESS.get(), pos, state, 2);
        this.tanks = new PressTanks(HorsePowerConfig.pressFluidTankSize.get());
        this.fluidHandler = new DualTankFluidHandler(tanks);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("currentPressStatus", currentPressStatus);
        tanks.serialize(output.child(TANKS_KEY));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child(TANKS_KEY).ifPresent(tanks::deserialize);

        // Legacy migration: pre-split saves used a single "fluid" key. Drop it into the
        // output tank if the fluid matches any recipe result (likely mid-extraction),
        // otherwise treat it as pending input.
        if (tanks.getResource(0).isEmpty() && tanks.getResource(1).isEmpty()) {
            input.read("fluid", FluidStack.CODEC).ifPresent(legacy -> {
                if (legacy.isEmpty()) return;
                int targetIndex = matchesAnyRecipeOutput(legacy)
                        ? DualTankFluidHandler.OUTPUT_INDEX
                        : DualTankFluidHandler.INPUT_INDEX;
                try (Transaction tx = Transaction.openRoot()) {
                    tanks.insert(targetIndex, FluidResource.of(legacy), legacy.getAmount(), tx);
                    tx.commit();
                }
            });
        }

        if (!getItem(0).isEmpty()) {
            currentPressStatus = input.getIntOr("currentPressStatus", 0);
        } else {
            currentPressStatus = 0;
        }
    }

    private boolean matchesAnyRecipeOutput(FluidStack fluid) {
        if (fluid.isEmpty() || !(level instanceof ServerLevel serverLevel)) return false;
        return ((RecipeManager) serverLevel.recipeAccess())
                .recipeMap().byType(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .anyMatch(r -> {
                    PressRecipe rec = r.value();
                    return rec.hasFluidOutput() && FluidStack.isSameFluidSameComponents(rec.getFluidResult(), fluid);
                });
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
        return getRecipe().map(r -> r.value().createResult()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeInputCount() {
        return getRecipe().map(r -> r.value().getInputCount()).orElse(1);
    }

    public Optional<RecipeHolder<PressRecipe>> getRecipe() {
        return findRecipe(HPRecipes.PRESSING_TYPE.get(), getItem(0));
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

            try (Transaction tx = Transaction.openRoot()) {
                recipe.getFluidInput().ifPresent(fluidIn -> {
                    FluidStack inputFluid = getInputFluid();
                    if (!inputFluid.isEmpty()) {
                        tanks.extract(DualTankFluidHandler.INPUT_INDEX,
                                FluidResource.of(inputFluid), fluidIn.amount(), tx);
                    }
                });

                if (recipe.hasFluidOutput()) {
                    FluidStack result = recipe.getFluidResult();
                    tanks.insert(DualTankFluidHandler.OUTPUT_INDEX,
                            FluidResource.of(result), result.getAmount(), tx);
                }
                tx.commit();
            }

            ItemStack itemResult = recipe.createResult();
            if (!itemResult.isEmpty()) {
                mergeOutput(1, itemResult);
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
        ItemStack result = recipe.createResult();

        if (getItem(0).getCount() < recipe.getInputCount()) {
            return false;
        }

        if (result.isEmpty() && !recipe.hasFluidOutput()) {
            return false;
        }

        FluidStack inputFluid = getInputFluid();
        if (recipe.hasFluidInput() && !recipe.getFluidInput().get().test(inputFluid)) {
            return false;
        }

        ItemStack output = getItem(1);
        if (!result.isEmpty()) {
            if (!output.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(output, result)) return false;
                if (output.getCount() + result.getCount() > output.getMaxStackSize()) return false;
            }
        }

        if (recipe.hasFluidOutput()) {
            FluidStack fluidOutput = recipe.getFluidResult();
            FluidStack currentOutput = getOutputFluid();
            // Output tank must be empty or already hold the same fluid, with room for the recipe result.
            if (!currentOutput.isEmpty() && !FluidStack.isSameFluidSameComponents(currentOutput, fluidOutput)) {
                return false;
            }
            int capacityFree = getTankCapacity() - currentOutput.getAmount();
            if (capacityFree < fluidOutput.getAmount()) return false;
        }

        return true;
    }

    @Override
    public int getInventoryStackLimit(ItemStack stack) {
        return this.<PressRecipe>findRecipe(HPRecipes.PRESSING_TYPE.get(), stack)
                .map(r -> r.value().getInputCount())
                .orElse(getInventoryStackLimit());
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
        if (!(level instanceof ServerLevel serverLevel)) return level != null && level.isClientSide();

        return ((RecipeManager) serverLevel.recipeAccess())
                .recipeMap().byType(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .anyMatch(recipe -> recipe.value().getIngredient().test(stack));
    }

    public FluidStack getInputFluid() {
        return tanks.getResource(DualTankFluidHandler.INPUT_INDEX)
                .toStack(tanks.getAmountAsInt(DualTankFluidHandler.INPUT_INDEX));
    }

    public FluidStack getOutputFluid() {
        return tanks.getResource(DualTankFluidHandler.OUTPUT_INDEX)
                .toStack(tanks.getAmountAsInt(DualTankFluidHandler.OUTPUT_INDEX));
    }

    public int getTankCapacity() {
        return tanks.getCapacityAsInt(DualTankFluidHandler.INPUT_INDEX, FluidResource.EMPTY);
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

    private class PressTanks extends FluidStacksResourceHandler {
        PressTanks(int capacity) {
            super(2, capacity);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChanged();
        }
    }
}
