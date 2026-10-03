package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeBatteryBlockEntity extends BlockEntity {

    private static final IEnergyStorage INFINITE = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return Math.max(maxExtract, 0);
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

    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> INFINITE);

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CREATIVE_BATTERY_BE.get(), pos, state);
    }

    public IEnergyStorage getEnergyHandler() {
        return INFINITE;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity be) {
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof CreativeBatteryBlockEntity) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                if (handler.canReceive()) {
                    handler.receiveEnergy(Integer.MAX_VALUE, false);
                }
            });
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCap = LazyOptional.of(() -> INFINITE);
    }
}
