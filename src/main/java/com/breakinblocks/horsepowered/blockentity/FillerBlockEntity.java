package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockFiller;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Tile entity for filler blocks that delegates to the main block below
 */
public class FillerBlockEntity extends BlockEntity {

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
}
