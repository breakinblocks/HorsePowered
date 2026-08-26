package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class GraniteAnvilBlockEntity extends HPBlockEntityBase {

    private int currentItemCrushAmount;
    private int totalItemCrushAmount;

    public GraniteAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRANITE_ANVIL.get(), pos, state, 2);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("crushTime", currentItemCrushAmount);
        output.putInt("totalCrushTime", totalItemCrushAmount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        if (!getItem(0).isEmpty()) {
            currentItemCrushAmount = input.getIntOr("crushTime", 0);
            totalItemCrushAmount = input.getIntOr("totalCrushTime", 1);
        } else {
            currentItemCrushAmount = 0;
            totalItemCrushAmount = 1;
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        ItemStack inputSlot = getItem(0);
        if (!inputSlot.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(inputSlot, stack)) return false;
            if (inputSlot.getCount() >= getInventoryStackLimit()) return false;
        }
        return findRecipe(HPRecipes.CRUSHING_TYPE.get(), stack).isPresent();
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

        ItemStack input = getItem(0);
        if (!level.isClientSide()) {
            ItemStack result = getRecipeOutput();
            if (HorsePowerConfig.graniteAnvilDrop.get()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(), result.copy());
            } else {
                mergeOutput(1, result);
            }
        }

        level.playSound(player, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
        input.shrink(1);
        setChanged();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.value().createResult()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(r -> r.value().getTime()).orElse(1);
    }

    public Optional<RecipeHolder<CrushingRecipe>> getRecipe() {
        return findRecipe(HPRecipes.CRUSHING_TYPE.get(), getItem(0));
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
