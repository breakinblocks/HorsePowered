package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
        itemStacks.set(slot, stack);

        if (slot == 0 && stack.getCount() > getMaxStackSize(stack)) {
            stack.setCount(getMaxStackSize(stack));
        }

        setChanged();
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
            // Output slots (1 and 2 for secondary)
            int outputSlot = getOutputSlot();
            if (outputSlot == 2) {
                return new int[]{1, 2};
            }
            return new int[]{1};
        } else {
            // Input slot
            return new int[]{0};
        }
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

    // Sync to client
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
        return ItemStack.isSameItemSameComponents(stack1, stack2) && stack1.getCount() <= stack1.getMaxStackSize();
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
