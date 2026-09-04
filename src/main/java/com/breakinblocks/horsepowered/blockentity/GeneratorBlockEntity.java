package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockGenerator;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class GeneratorBlockEntity extends HPBlockEntityHorseBase {

    public static final int MAX_ENERGY = 100_000;
    public static final int FE_PER_TICK = 80;

    private final GeneratorEnergy energy = new GeneratorEnergy();

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GENERATOR_BE.get(), pos, state, 0);
    }

    public IEnergyStorage getEnergyHandler() {
        return energy;
    }

    @Override
    protected void tickServer() {
        super.tickServer();

        boolean working = worker.isWorking();

        if (working) {
            int stored = energy.getEnergyStored();
            if (stored < MAX_ENERGY) {
                energy.generate(Math.min(FE_PER_TICK, MAX_ENERGY - stored));
            }
        }

        if (level != null && !level.isClientSide && energy.getEnergyStored() > 0) {
            pushEnergyToNeighbors();
        }

        BlockState state = getBlockState();
        if (state.hasProperty(BlockGenerator.POWERED) && state.getValue(BlockGenerator.POWERED) != working) {
            level.setBlock(worldPosition, state.setValue(BlockGenerator.POWERED, working), 3);
        }
    }

    private void pushEnergyToNeighbors() {
        for (Direction dir : Direction.values()) {
            int stored = energy.getEnergyStored();
            if (stored <= 0) return;

            IEnergyStorage neighbor = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    worldPosition.relative(dir),
                    dir.getOpposite());
            if (neighbor == null || !neighbor.canReceive()) continue;

            int simulated = neighbor.receiveEnergy(stored, true);
            if (simulated <= 0) continue;
            int accepted = neighbor.receiveEnergy(simulated, false);
            if (accepted > 0) {
                energy.extractEnergy(accepted, false);
            }
        }
    }

    @Override
    public boolean canWork() {
        if (level == null) return false;
        if (!level.hasNeighborSignal(worldPosition)) return false;
        return energy.getEnergyStored() < MAX_ENERGY;
    }

    @Override
    public boolean targetReached() {
        return true;
    }

    @Override
    public int getPositionOffset() {
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setStored(tag.getInt("energy"));
    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getOutputSlot() {
        return -1;
    }

    private void markDirtyForSave() {
        if (level != null) {
            level.blockEntityChanged(worldPosition);
        }
    }

    private final class GeneratorEnergy extends EnergyStorage {
        GeneratorEnergy() {
            super(MAX_ENERGY, 0, MAX_ENERGY);
        }

        void generate(int amount) {
            if (amount <= 0) return;
            int previous = this.energy;
            this.energy = Math.min(this.capacity, this.energy + amount);
            if (this.energy != previous) {
                markDirtyForSave();
            }
        }

        void setStored(int amount) {
            this.energy = Math.max(0, Math.min(this.capacity, amount));
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                markDirtyForSave();
            }
            return extracted;
        }
    }
}
