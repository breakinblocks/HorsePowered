package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenHopperBlockEntity extends HopperBlockEntity {

    private int speedTickCounter = 0;

    public WoodenHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.WOODEN_HOPPER.get();
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    public static void pushItemsTick(Level level, BlockPos pos, BlockState state, WoodenHopperBlockEntity be) {
        be.speedTickCounter++;
        if ((be.speedTickCounter & 1) == 0) {
            HopperBlockEntity.pushItemsTick(level, pos, state, be);
        }
    }
}
