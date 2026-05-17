package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.CreativeBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockCreativeBattery extends Block implements EntityBlock {

    public BlockCreativeBattery(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeBatteryBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlocks.CREATIVE_BATTERY_BE.get()
                ? (BlockEntityTicker<T>) (BlockEntityTicker<CreativeBatteryBlockEntity>) CreativeBatteryBlockEntity::serverTick
                : null;
    }
}
