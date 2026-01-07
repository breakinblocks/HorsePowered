package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;

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
        return getRecipe().map(r -> r.value().getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getRecipeSecondary() {
        return getRecipe().map(r -> r.value().getSecondary().copy()).orElse(ItemStack.EMPTY);
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
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        HPRecipeInput recipeInput = new HPRecipeInput(getItem(0));
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), recipeInput, serverLevel);
    }

    private void millItem() {
        if (level != null && !level.isClientSide() && canWork()) {
            Optional<RecipeHolder<GrindstoneRecipe>> recipeOpt = getRecipe();
            if (recipeOpt.isEmpty()) return;

            GrindstoneRecipe recipe = recipeOpt.get().value();
            ItemStack result = recipe.getResult();
            ItemStack secondary = recipe.getSecondary();

            ItemStack input = getItem(0);
            ItemStack output = getItem(1);
            ItemStack secondaryOutput = getItem(2);

            // Process main output
            if (output.isEmpty()) {
                setItem(1, result.copy());
            } else if (ItemStack.isSameItemSameComponents(output, result)) {
                output.grow(result.getCount());
            }

            // Process secondary output
            processSecondaries(level, secondary, secondaryOutput, recipe.getSecondaryChance());

            input.shrink(1);
            setChanged();
        }
    }

    private void processSecondaries(Level world, ItemStack secondary, ItemStack secondaryOutput, int chance) {
        if (!secondary.isEmpty()) {
            if (chance >= 100 || world.random.nextInt(100) < chance) {
                if (secondaryOutput.isEmpty()) {
                    setItem(2, secondary.copy());
                } else if (ItemStack.isSameItemSameComponents(secondaryOutput, secondary)) {
                    secondaryOutput.grow(secondary.getCount());
                }
            }
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
    public void setItem(int slot, ItemStack stack) {
        ItemStack oldStack = getItem(slot);
        super.setItem(slot, stack);

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, oldStack);
        if (slot == 0 && !isSameItem) {
            totalItemMillTime = getRecipeTime();
            currentItemMillTime = 0;
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        if (!(level instanceof ServerLevel serverLevel)) return false;

        HPRecipeInput recipeInput = new HPRecipeInput(stack);
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), recipeInput, serverLevel)
                .isPresent();
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
