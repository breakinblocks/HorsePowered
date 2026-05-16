package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CreativeBatteryBlockEntity extends BlockEntity {

    private final EnergyStorage energy = new EnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

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

            IEnergyStorage neighbor = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos.relative(dir),
                    dir.getOpposite());
            if (neighbor == null || !neighbor.canReceive()) continue;

            int simulated = neighbor.receiveEnergy(stored, true);
            if (simulated <= 0) continue;
            int accepted = neighbor.receiveEnergy(simulated, false);
            if (accepted > 0) {
                be.energy.extractEnergy(accepted, false);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int stored = tag.getInt("energy");
        if (stored > 0) {
            energy.receiveEnergy(stored, false);
        }
    }
}
