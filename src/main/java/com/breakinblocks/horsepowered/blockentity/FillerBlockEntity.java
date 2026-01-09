package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockFiller;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Tile entity for filler blocks that delegates to the main block below.
 * Implements WorldlyContainer to allow hopper interaction with the main block.
 */
public class FillerBlockEntity extends BlockEntity implements WorldlyContainer {

    public FillerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILLER.get(), pos, state);
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

    // WorldlyContainer implementation - delegates to the main block entity

    @Override
    public int[] getSlotsForFace(Direction side) {
        HPBlockEntityBase te = getFilledTileEntity();
        return te != null ? te.getSlotsForFace(side) : new int[0];
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
}
