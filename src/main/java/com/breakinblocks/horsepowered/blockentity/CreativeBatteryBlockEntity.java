package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CreativeBatteryBlockEntity extends BlockEntity {

    private static final IEnergyStorage INFINITE = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return Math.max(toExtract, 0);
        }

        @Override
        public int getEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CREATIVE_BATTERY_BE.get(), pos, state);
    }

    public IEnergyStorage getEnergyHandler() {
        return INFINITE;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity be) {
        for (Direction dir : Direction.values()) {
            IEnergyStorage neighbor =
                    level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(dir), dir.getOpposite());
            if (neighbor == null || neighbor == INFINITE || !neighbor.canReceive()) continue;

            neighbor.receiveEnergy(Integer.MAX_VALUE, false);
        }
    }
}
