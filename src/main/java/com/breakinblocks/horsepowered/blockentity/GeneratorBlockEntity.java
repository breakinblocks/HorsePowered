package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockGenerator;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlockEntity extends HPBlockEntityHorseBase {

    public static final int MAX_ENERGY = 100_000;
    public static final int FE_PER_TICK = 80;

    private final GeneratorEnergy energy = new GeneratorEnergy();
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GENERATOR_BE.get(), pos, state, 0);
    }

    public IEnergyStorage getEnergyHandler() {
        return energy;
    }

    @Override
    protected void tickServer() {
        super.tickServer();

        boolean working = running && valid && hasWorker();

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

            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                if (!handler.canReceive()) return;
                int simulated = handler.receiveEnergy(stored, true);
                if (simulated <= 0) return;
                int accepted = handler.receiveEnergy(simulated, false);
                if (accepted > 0) {
                    energy.extractEnergy(accepted, false);
                }
            });
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
