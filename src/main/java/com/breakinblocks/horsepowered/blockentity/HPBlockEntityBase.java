package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;

import java.util.Optional;

public abstract class HPBlockEntityBase extends BlockEntity implements Container, WorldlyContainer {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int[] SLOTS_DOWN_DUAL = {1, 2};
    private static final int[] SLOTS_DOWN_SINGLE = {1};
    private static final int[] SLOTS_INPUT = {0};

    protected NonNullList<ItemStack> itemStacks;
    protected Direction forward = Direction.NORTH;

    public HPBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state);
        this.itemStacks = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    }

    // Abstract methods to be implemented by subclasses
    public abstract int getInventoryStackLimit();

    public abstract boolean isItemValidForSlot(int index, ItemStack stack);

    public abstract int getOutputSlot();

    public int getInventoryStackLimit(ItemStack stack) {
        return getInventoryStackLimit();
    }

    // Container implementation
    @Override
    public int getContainerSize() {
        return itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : itemStacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= itemStacks.size()) return ItemStack.EMPTY;
        return itemStacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(itemStacks, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(itemStacks, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack oldStack = getItem(slot);
        itemStacks.set(slot, stack);

        if (slot == 0 && stack.getCount() > getMaxStackSize(stack)) {
            stack.setCount(getMaxStackSize(stack));
        }

        if (slot == 0 && !ItemStack.isSameItemSameComponents(oldStack, stack)) {
            onInputChanged();
        }

        setChanged();
    }

    /**
     * Called when slot 0 changes to a different item type.
     * Override in subclasses to reset progress counters.
     */
    protected void onInputChanged() {
    }

    public int getMaxStackSize(ItemStack stack) {
        return Math.min(getInventoryStackLimit(stack), stack.getMaxStackSize());
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        itemStacks.clear();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isItemValidForSlot(index, stack);
    }

    // WorldlyContainer implementation for automation
    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return getOutputSlot() == 2 ? SLOTS_DOWN_DUAL : SLOTS_DOWN_SINGLE;
        }
        return SLOTS_INPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return index == 0 && isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index > 0; // Only output slots
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemStacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, itemStacks);

        if (canBeRotated()) {
            String forwardName = input.getStringOr("forward", "north");
            Direction dir = Direction.byName(forwardName);
            forward = dir != null ? dir : Direction.NORTH;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, itemStacks);

        if (canBeRotated()) {
            output.putString("forward", forward.getName());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    public boolean canWork() {
        if (getItem(0).isEmpty()) {
            return false;
        }

        ItemStack output = getRecipeOutput();
        ItemStack secondary = getRecipeSecondary();

        if (output.isEmpty()) {
            return false;
        }

        int inputRequired = getRecipeInputCount();
        if (getItem(0).getCount() < inputRequired) {
            return false;
        }

        ItemStack currentOutput = getItem(1);
        if (!currentOutput.isEmpty()) {
            if (!canCombine(currentOutput, output)) {
                return false;
            }
            if (currentOutput.getCount() + output.getCount() > currentOutput.getMaxStackSize()) {
                return false;
            }
        }

        if (!secondary.isEmpty()) {
            ItemStack currentSecondary = getItem(2);
            if (!currentSecondary.isEmpty()) {
                if (!canCombine(currentSecondary, secondary)) {
                    return false;
                }
                if (currentSecondary.getCount() + secondary.getCount() > secondary.getMaxStackSize()) {
                    return false;
                }
            }
        }

        return true;
    }

    // Methods to be overridden for recipe lookup
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    public ItemStack getRecipeSecondary() {
        return ItemStack.EMPTY;
    }

    public int getRecipeSecondaryChance() {
        return 0;
    }

    public int getRecipeInputCount() {
        return 1;
    }

    public int getRecipeTime() {
        return 0;
    }

    public static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    /**
     * Merges a result stack into the given output slot. If the slot is empty the
     * result is placed directly; otherwise the count is grown when compatible.
     */
    protected void mergeOutput(int slot, ItemStack result) {
        ItemStack current = getItem(slot);
        if (current.isEmpty()) {
            setItem(slot, result.copy());
        } else if (ItemStack.isSameItemSameComponents(current, result)) {
            // grow() mutates the stack in place — setItem isn't called so we have to
            // setChanged ourselves or the new count never syncs to the client and
            // tools like Jade see the stale pre-merge count.
            current.grow(result.getCount());
            setChanged();
        }
    }

    /**
     * Rolls the chance for a secondary output and merges it into slot 2.
     */
    protected void processSecondary(ItemStack secondary, int chance) {
        if (!secondary.isEmpty() && level != null) {
            if (chance >= 100 || level.getRandom().nextInt(100) < chance) {
                mergeOutput(2, secondary);
            }
        }
    }

    /**
     * Looks up a recipe of the given type for the specified input item.
     * Centralizes the common recipe lookup pattern used across all block entities.
     */
    protected <T extends Recipe<HPRecipeInput>> Optional<RecipeHolder<T>> findRecipe(RecipeType<T> type, ItemStack input) {
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        try {
            Optional<RecipeHolder<T>> result = ((RecipeManager) serverLevel.recipeAccess())
                    .getRecipeFor(type, new HPRecipeInput(input), serverLevel);
            if (result.isEmpty()) {
                LOGGER.debug("[HorsePowered] No recipe found for type {} with input {}", type, input);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("[HorsePowered] Recipe lookup failed for type {} with input {}", type, input, e);
            return Optional.empty();
        }
    }

    // Rotation support
    public boolean canBeRotated() {
        return false;
    }

    public Direction getForward() {
        return forward;
    }

    public void setForward(Direction forward) {
        this.forward = forward;
    }
}
