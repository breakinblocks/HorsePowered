package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockPress extends BlockHPBase {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BlockPress(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
        // No special action needed
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PressBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> HPBlockEntityHorseBase.clientTick(lvl, pos, st, (PressBlockEntity) be);
        } else {
            return (lvl, pos, st, be) -> HPBlockEntityHorseBase.serverTick(lvl, pos, st, (PressBlockEntity) be);
        }
    }
}
