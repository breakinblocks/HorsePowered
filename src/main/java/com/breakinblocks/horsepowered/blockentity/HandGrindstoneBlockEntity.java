package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        super(ModBlocks.HAND_GRINDSTONE_BE.get(), pos, state, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("millTime", currentItemMillTime);
        tag.putInt("totalMillTime", totalItemMillTime);
        tag.putInt("currentRotation", rotation);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (!getItem(0).isEmpty()) {
            currentItemMillTime = tag.getInt("millTime");
            totalItemMillTime = tag.getInt("totalMillTime");
            rotation = tag.getInt("currentRotation");
        } else {
            currentItemMillTime = 0;
            totalItemMillTime = 1;
            rotation = 0;
        }
    }

    @Override
    public ItemStack getRecipeOutput() {
        return getRecipe().map(r -> r.getResult().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getRecipeSecondary() {
        return getRecipe().map(r -> r.getSecondary().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getRecipeSecondaryChance() {
        return getRecipe().map(GrindstoneRecipe::getSecondaryChance).orElse(0);
    }

    @Override
    public int getRecipeTime() {
        return getRecipe().map(GrindstoneRecipe::getTime).orElse(0);
    }

    public Optional<GrindstoneRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        SimpleContainer container = new SimpleContainer(getItem(0));
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), container, level);
    }

    private void millItem() {
        if (level != null && !level.isClientSide && canWork()) {
            Optional<GrindstoneRecipe> recipeOpt = getRecipe();
            if (recipeOpt.isEmpty()) return;

            GrindstoneRecipe recipe = recipeOpt.get();
            ItemStack result = recipe.getResult();
            ItemStack secondary = recipe.getSecondary();

            ItemStack input = getItem(0);
            ItemStack output = getItem(1);
            ItemStack secondaryOutput = getItem(2);

            // Process main output
            if (output.isEmpty()) {
                setItem(1, result.copy());
            } else if (ItemStack.isSameItemSameTags(output, result)) {
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
                } else if (ItemStack.isSameItemSameTags(secondaryOutput, secondary)) {
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

        boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, oldStack);
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
        if (level == null) return false;

        SimpleContainer container = new SimpleContainer(stack);
        return level.getRecipeManager()
                .getRecipeFor(HPRecipes.GRINDING_TYPE.get(), container, level)
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
        if (level == null || level.isClientSide) return false;

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

                currentItemMillTime += Configs.pointsPerRotation.get();

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
