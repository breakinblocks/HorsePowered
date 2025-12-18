package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ManualChopperBlockEntity extends HPBlockEntityBase {

    private int currentItemChopAmount;
    private int totalItemChopAmount;

    public ManualChopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CHOPPING_BLOCK_BE.get(), pos, state, 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("chopTime", currentItemChopAmount);
        tag.putInt("totalChopTime", totalItemChopAmount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (!getItem(0).isEmpty()) {
            currentItemChopAmount = tag.getInt("chopTime");
            totalItemChopAmount = tag.getInt("totalChopTime");
        } else {
            currentItemChopAmount = 0;
            totalItemChopAmount = 1;
        }
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

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, oldStack);
        if (slot == 0 && !isSameItem) {
            totalItemChopAmount = getRecipeTime() * Configs.choppingMultiplier.get();
            currentItemChopAmount = 0;
        }
    }

    private void chopItem(Player player) {
        if (level == null || !canWork()) return;

        ItemStack input = getItem(0);
        if (!level.isClientSide) {
            ItemStack result = getRecipeOutput();
            ItemStack output = getItem(1);

            if (Configs.choppingBlockDrop.get()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(), result.copy());
            } else {
                if (output.isEmpty()) {
                    setItem(1, result.copy());
                } else if (ItemStack.isSameItemSameTags(output, result)) {
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
