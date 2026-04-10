package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockFiller;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for filler blocks in multi-block structures.
 * Forwards all container and setChanged calls to the main block entity below,
 * so hoppers and other automation can insert items through the filler (e.g. the
 * top block of the chopper).
 */
public class FillerBlockEntity extends BlockEntity implements WorldlyContainer {

    private static final int[] NO_SLOTS = new int[0];

    public FillerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FILLER_BE.get(), pos, state);
    }

    /**
     * Gets the main block entity that this filler is paired with
     */
    @Nullable
    public HPBlockEntityBase getFilledTileEntity() {
        if (level == null) return null;
        BlockPos filledPos = getFilledPos();
        BlockEntity tileEntity = level.getBlockEntity(filledPos);
        if (tileEntity instanceof HPBlockEntityBase) {
            return (HPBlockEntityBase) tileEntity;
        }
        return null;
    }

    /**
     * Gets the position of the main block this filler is paired with
     */
    public BlockPos getFilledPos() {
        if (level == null) return worldPosition;
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BlockFiller)) return worldPosition;
        Direction facing = state.getValue(BlockFiller.FACING);
        return worldPosition.relative(facing);
    }

    @Override
    public void setChanged() {
        HPBlockEntityBase te = getFilledTileEntity();
        if (te != null) {
            te.setChanged();
        }
        super.setChanged();
    }

    // ==================== WorldlyContainer delegation ====================
    // All calls forward to the main block entity so hoppers can interact with
    // the filler block (e.g. dropping logs into the top of the chopper).

    @Override
    public int[] getSlotsForFace(Direction side) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null ? te.getSlotsForFace(side) : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null && te.canPlaceItemThroughFace(index, stack, direction);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null && te.canTakeItemThroughFace(index, stack, direction);
    }

    @Override
    public int getContainerSize() {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null ? te.getContainerSize() : 0;
    }

    @Override
    public boolean isEmpty() {
        HPBlockEntityBase te = getFilledTileEntity();
        return te == null || te.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null ? te.getItem(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null ? te.removeItem(slot, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null ? te.removeItemNoUpdate(slot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        HPBlockEntityBase te = getFilledTileEntity();
        if (te != null) {
            te.setItem(slot, stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null ? te.getMaxStackSize() : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null && te.stillValid(player);
    }

    @Override
    public void clearContent() {
        HPBlockEntityBase te = getFilledTileEntity();
        if (te != null) {
            te.clearContent();
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null && te.canPlaceItem(index, stack);
    }
}
