package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.Optional;

// TODO: Migrate to ResourceHandler<FluidResource> when FluidTank is removed in NeoForge 21.10+
@SuppressWarnings("removal")
public class PressBlockEntity extends HPBlockEntityHorseBase {

    private final FluidTank tank;
    private int currentPressStatus;

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESS.get(), pos, state, 2);
        this.tank = new FluidTank(HorsePowerConfig.pressFluidTankSize.get()) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("currentPressStatus", currentPressStatus);
        // Save fluid as CompoundTag using codec
        if (!tank.isEmpty()) {
            FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, tank.getFluid())
                    .resultOrPartial(e -> {})
                    .ifPresent(tag -> {
                        if (tag instanceof CompoundTag compoundTag) {
                            output.store("fluid", CompoundTag.CODEC, compoundTag);
                        }
                    });
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        // Load fluid from CompoundTag using codec
        input.read("fluid", CompoundTag.CODEC).ifPresent(tag -> {
            FluidStack.CODEC.parse(NbtOps.INSTANCE, tag)
                    .resultOrPartial(e -> {})
                    .ifPresent(fluidStack -> tank.setFluid(fluidStack));
        });

        if (!getItem(0).isEmpty()) {
            currentPressStatus = input.getIntOr("currentPressStatus", 0);
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
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        HPRecipeInput recipeInput = new HPRecipeInput(getItem(0));
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.PRESSING_TYPE.get(), recipeInput, serverLevel);
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
            ItemStack result = recipe.getResult();
            FluidStack fluidResult = recipe.getFluidResult();

            ItemStack input = getItem(0);
            ItemStack output = getItem(1);

            if (recipe.hasFluidOutput()) {
                tank.fill(fluidResult.copy(), IFluidHandler.FluidAction.EXECUTE);
            } else {
                if (output.isEmpty()) {
                    setItem(1, result.copy());
                } else if (ItemStack.isSameItemSameComponents(output, result)) {
                    output.grow(result.getCount());
                }
            }

            input.shrink(recipe.getInputCount());
            setChanged();
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack oldStack = getItem(slot);
        super.setItem(slot, stack);

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, oldStack);
        if (slot == 0 && !isSameItem) {
            currentPressStatus = 0;
        }
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
            // For fluid output, output slot must be empty and tank must have room
            return output.isEmpty() &&
                    (tank.getFluidAmount() == 0 || tank.fill(fluidOutput.copy(), IFluidHandler.FluidAction.SIMULATE) >= fluidOutput.getAmount());
        } else {
            // For item output, tank must be empty and output slot must have room
            return tank.getFluidAmount() == 0 &&
                    (output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result) &&
                            output.getCount() + result.getCount() <= output.getMaxStackSize()));
        }
    }

    @Override
    public int getInventoryStackLimit(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) return getInventoryStackLimit();

        HPRecipeInput recipeInput = new HPRecipeInput(stack);
        Optional<RecipeHolder<PressRecipe>> recipeOpt = ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.PRESSING_TYPE.get(), recipeInput, serverLevel);

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
        // Only reject if pressing is in progress (don't reject just because output has items)
        if (currentPressStatus != 0) return false;
        if (!(level instanceof ServerLevel serverLevel)) return false;

        // Check if ANY press recipe accepts this item type (ignore count requirement)
        // This allows hoppers to insert items one at a time
        return ((RecipeManager) serverLevel.recipeAccess())
                .recipeMap().byType(HPRecipes.PRESSING_TYPE.get())
                .stream()
                .anyMatch(recipe -> recipe.value().getIngredient().test(stack));
    }

    public FluidTank getTank() {
        return tank;
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
