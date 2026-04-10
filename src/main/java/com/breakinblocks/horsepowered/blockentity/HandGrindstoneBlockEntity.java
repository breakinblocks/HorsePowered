package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class HandGrindstoneBlockEntity extends HPBlockEntityBase {

    private int currentItemMillTime;
    private int totalItemMillTime;

    private static final int TICKS_PER_ROTATION = 18;
    private float visibleRotation = 0;
    private int currentTicks = 0;
    private int rotation = 0;

    public HandGrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAND_GRINDSTONE.get(), pos, state, 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("millTime", currentItemMillTime);
        output.putInt("totalMillTime", totalItemMillTime);
        output.putInt("currentRotation", rotation);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        if (!getItem(0).isEmpty()) {
            currentItemMillTime = input.getIntOr("millTime", 0);
            totalItemMillTime = input.getIntOr("totalMillTime", 1);
            rotation = input.getIntOr("currentRotation", 0);
        } else {
            currentItemMillTime = 0;
            totalItemMillTime = 1;
            rotation = 0;
        }
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.value().createResult()).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getRecipeSecondary() {
        return getRecipe().map(r -> r.value().createSecondary()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeSecondaryChance() {
        return getRecipe().map(r -> r.value().getSecondaryChance()).orElse(0);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(r -> r.value().getTime()).orElse(0);
    }

    public Optional<RecipeHolder<GrindstoneRecipe>> getRecipe() {
        return findRecipe(HPRecipes.GRINDING_TYPE.get(), getItem(0));
    }

    private void millItem() {
        if (level != null && !level.isClientSide() && canWork()) {
            Optional<RecipeHolder<GrindstoneRecipe>> recipeOpt = getRecipe();
            if (recipeOpt.isEmpty()) return;

            GrindstoneRecipe recipe = recipeOpt.get().value();

            mergeOutput(1, recipe.createResult());
            processSecondary(recipe.createSecondary(), recipe.getSecondaryChance());

            getItem(0).shrink(1);
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (getItem(0).isEmpty()) {
            currentItemMillTime = 0;
        }
    }

    @Override
    protected void onInputChanged() {
        totalItemMillTime = getRecipeTime();
        currentItemMillTime = 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        // Recipe lookup is server-only; on the client, allow insertion so the
        // interaction isn't blocked (server will do the authoritative check)
        if (level != null && level.isClientSide()) return true;
        return findRecipe(HPRecipes.GRINDING_TYPE.get(), stack).isPresent();
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

    /**
     * Called when the player turns the grindstone
     * @return true if the grindstone was turned
     */
    public boolean turn() {
        if (level == null || level.isClientSide()) return false;

        if (rotation < 3 && canWork()) {
            rotation += TICKS_PER_ROTATION;
            setChanged();
            return true;
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HandGrindstoneBlockEntity blockEntity) {
        blockEntity.tickServer();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HandGrindstoneBlockEntity blockEntity) {
        blockEntity.tickClient();
    }

    private void tickServer() {
        if (rotation > 0) {
            currentTicks++;
            if (currentTicks >= TICKS_PER_ROTATION) {
                currentTicks -= TICKS_PER_ROTATION;

                currentItemMillTime += HorsePowerConfig.pointsPerRotation.get();

                if (currentItemMillTime >= totalItemMillTime) {
                    currentItemMillTime = 0;
                    millItem();
                    totalItemMillTime = getRecipeTime();
                }
                setChanged();
            }
            rotation--;
        }
    }

    private void tickClient() {
        if (rotation > 0) {
            visibleRotation = (visibleRotation - 360f / TICKS_PER_ROTATION) % -360;
            rotation--;
        } else {
            visibleRotation = 0;
        }
    }

    public float getVisibleRotation() {
        return visibleRotation;
    }

    @Override
    public boolean canBeRotated() {
        return true;
    }
}
