package com.breakinblocks.horsepowered.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * A liquid block that is flammable — fire will spread to and through it.
 * Used for seed oil so that lava/fire causes chain ignition.
 */
public class FlammableLiquidBlock extends LiquidBlock {

    private final int flammability;
    private final int fireSpreadSpeed;

    public FlammableLiquidBlock(FlowingFluid fluid, Properties properties, int flammability, int fireSpreadSpeed) {
        super(fluid, properties);
        this.flammability = flammability;
        this.fireSpreadSpeed = fireSpreadSpeed;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return flammability;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return fireSpreadSpeed;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }
}
