package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class GraniteAnvilBlockEntity extends HPBlockEntityBase {

    private int currentItemCrushAmount;
    private int totalItemCrushAmount;

    public GraniteAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GRANITE_ANVIL_BE.get(), pos, state, 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("crushTime", currentItemCrushAmount);
        tag.putInt("totalCrushTime", totalItemCrushAmount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (!getItem(0).isEmpty()) {
            currentItemCrushAmount = tag.getInt("crushTime");
            totalItemCrushAmount = tag.getInt("totalCrushTime");
        } else {
            currentItemCrushAmount = 0;
            totalItemCrushAmount = 1;
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        if (!getItem(1).isEmpty() || !getItem(0).isEmpty()) return false;
        if (level == null) return false;

        HPRecipeInput input = new HPRecipeInput(stack);
        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.CRUSHING_TYPE.get()).stream()
                .anyMatch(r -> r.value().matches(input, level));
    }

    public boolean crush(Player player, ItemStack held) {
        if (canWork()) {
            currentItemCrushAmount++;

            if (currentItemCrushAmount >= totalItemCrushAmount) {
                currentItemCrushAmount = 0;
                totalItemCrushAmount = getRecipeTime() * HorsePowerConfig.crushingMultiplier.get();
                crushItem(player);
                return true;
            }
            setChanged();
        }
        return false;
    }

    @Override
    protected void onInputChanged() {
        totalItemCrushAmount = getRecipeTime() * HorsePowerConfig.crushingMultiplier.get();
        currentItemCrushAmount = 0;
    }

    private void crushItem(Player player) {
        if (level == null || !canWork()) return;

        if (!level.isClientSide) {
            ItemStack result = getRecipeOutput();
            if (HorsePowerConfig.graniteAnvilDrop.get()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(), result.copy());
            } else {
                mergeOutput(1, result);
            }
        }

        level.playSound(player, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
        getItem(0).shrink(1);
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

    public Optional<RecipeHolder<CrushingRecipe>> getRecipe() {
        if (level == null) return Optional.empty();
        HPRecipeInput input = new HPRecipeInput(getItem(0));
        return level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.CRUSHING_TYPE.get()).stream()
                .filter(r -> r.value().matches(input, level))
                .findFirst();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public int getOutputSlot() {
        return 1;
    }

    public int getCurrentCrushAmount() {
        return currentItemCrushAmount;
    }

    public int getTotalCrushAmount() {
        return totalItemCrushAmount;
    }
}
