package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeBatteryBlockEntity extends BlockEntity {

    private final EnergyStorage energy =
            new EnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CREATIVE_BATTERY_BE.get(), pos, state);
    }

    public IEnergyStorage getEnergyHandler() {
        return energy;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity be) {
        if (be.energy.getEnergyStored() <= 0) return;

        for (Direction dir : Direction.values()) {
            int stored = be.energy.getEnergyStored();
            if (stored <= 0) return;

            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                if (!handler.canReceive()) return;
                int simulated = handler.receiveEnergy(stored, true);
                if (simulated <= 0) return;
                int accepted = handler.receiveEnergy(simulated, false);
                if (accepted > 0) {
                    be.energy.extractEnergy(accepted, false);
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
        energyCap = LazyOptional.of(() -> energy);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int stored = tag.getInt("energy");
        if (stored > 0) {
            energy.receiveEnergy(stored, false);
        }
    }
}
