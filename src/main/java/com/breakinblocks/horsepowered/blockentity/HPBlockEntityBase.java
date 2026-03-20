package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class HPBlockEntityBase extends BlockEntity implements Container, WorldlyContainer {

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

        if (slot == 0) {
            boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, oldStack);
            if (!isSameItem) {
                onInputChanged();
            }
        }

        setChanged();
    }

    /**
     * Called when the input slot (slot 0) changes to a different item type.
     * Subclasses should override this to reset progress timers.
     */
    protected void onInputChanged() {
        // Default: no-op. Subclasses override to reset progress.
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

    // NBT serialization
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        itemStacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, itemStacks);

        if (canBeRotated() && tag.contains("forward")) {
            forward = Direction.byName(tag.getString("forward"));
            if (forward == null) forward = Direction.NORTH;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, itemStacks);

        if (canBeRotated()) {
            tag.putString("forward", forward.getName());
        }
    }

    // Sync to client
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    // Recipe and work logic
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

        // Check if output slot can accept result
        ItemStack currentOutput = getItem(1);
        if (!currentOutput.isEmpty()) {
            if (!canCombine(currentOutput, output)) {
                return false;
            }
            if (currentOutput.getCount() + output.getCount() > currentOutput.getMaxStackSize()) {
                return false;
            }
        }

        // Check if secondary slot can accept result
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
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }

    /**
     * Merges a result stack into the given output slot.
     * @return true if the merge was successful
     */
    protected boolean mergeOutput(int slot, ItemStack result) {
        ItemStack existing = getItem(slot);
        if (existing.isEmpty()) {
            setItem(slot, result.copy());
            return true;
        } else if (ItemStack.isSameItemSameTags(existing, result)) {
            existing.grow(result.getCount());
            return true;
        }
        return false;
    }

    /**
     * Processes a secondary output with a chance roll.
     */
    protected void processSecondary(ItemStack secondary, int chance) {
        if (!secondary.isEmpty() && level != null) {
            if (chance >= 100 || level.random.nextInt(100) < chance) {
                mergeOutput(2, secondary);
            }
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
