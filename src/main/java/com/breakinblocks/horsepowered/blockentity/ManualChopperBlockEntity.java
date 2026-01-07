package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ManualChopperBlockEntity extends HPBlockEntityBase {

    private int currentItemChopAmount;
    private int totalItemChopAmount;

    public ManualChopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHOPPING_BLOCK.get(), pos, state, 2);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("chopTime", currentItemChopAmount);
        output.putInt("totalChopTime", totalItemChopAmount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        if (!getItem(0).isEmpty()) {
            currentItemChopAmount = input.getIntOr("chopTime", 0);
            totalItemChopAmount = input.getIntOr("totalChopTime", 1);
        } else {
            currentItemChopAmount = 0;
            totalItemChopAmount = 1;
        }
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

    /**
     * Called when the player chops with an axe
     * @return true if the chop completed an item
     */
    public boolean chop(Player player, ItemStack held) {
        if (canWork()) {
            currentItemChopAmount++;

            if (currentItemChopAmount >= totalItemChopAmount) {
                currentItemChopAmount = 0;
                totalItemChopAmount = getRecipeTime();
                chopItem(player);
                return true;
            }
            setChanged();
        }
        return false;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack oldStack = getItem(slot);
        super.setItem(slot, stack);

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, oldStack);
        if (slot == 0 && !isSameItem) {
            totalItemChopAmount = getRecipeTime() * HorsePowerConfig.choppingMultiplier.get();
            currentItemChopAmount = 0;
        }
    }

    private void chopItem(Player player) {
        if (level == null || !canWork()) return;

        ItemStack input = getItem(0);
        if (!level.isClientSide()) {
            ItemStack result = getRecipeOutput();
            ItemStack output = getItem(1);

            if (HorsePowerConfig.choppingBlockDrop.get()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(), result.copy());
            } else {
                if (output.isEmpty()) {
                    setItem(1, result.copy());
                } else if (ItemStack.isSameItemSameComponents(output, result)) {
                    output.grow(result.getCount());
                }
            }
        }

        level.playSound(player, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
        input.shrink(1);
        setChanged();
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
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public int getOutputSlot() {
        return 1;
    }

    public int getCurrentChopAmount() {
        return currentItemChopAmount;
    }

    public int getTotalChopAmount() {
        return totalItemChopAmount;
    }
}
