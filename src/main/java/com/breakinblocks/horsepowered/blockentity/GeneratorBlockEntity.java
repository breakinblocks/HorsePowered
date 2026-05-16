package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class GeneratorBlockEntity extends HPBlockEntityHorseBase {

    public static final int MAX_ENERGY = 100_000;
    public static final int FE_PER_TICK = 80;

    private final GeneratorEnergy energy = new GeneratorEnergy();

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR.get(), pos, state, 0);
    }

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    @Override
    protected void tickServer() {
        super.tickServer();

        boolean working = running && valid && hasVirtualWorker;

        if (working) {
            int stored = energy.getAmountAsInt();
            if (stored < MAX_ENERGY) {
                energy.generate(Math.min(FE_PER_TICK, MAX_ENERGY - stored));
            }
        }

        if (level != null && !level.isClientSide() && energy.getAmountAsInt() > 0) {
            pushEnergyToNeighbors();
        }

        BlockState state = getBlockState();
        if (state.hasProperty(BlockGenerator.POWERED) && state.getValue(BlockGenerator.POWERED) != working) {
            level.setBlock(worldPosition, state.setValue(BlockGenerator.POWERED, working), 3);
        }
    }

    private void pushEnergyToNeighbors() {
        for (Direction dir : Direction.values()) {
            int stored = energy.getAmountAsInt();
            if (stored <= 0) return;

            EnergyHandler neighbor = level.getCapability(
                    Capabilities.Energy.BLOCK,
                    worldPosition.relative(dir),
                    dir.getOpposite());
            if (neighbor == null) continue;

            try (Transaction tx = Transaction.openRoot()) {
                int inserted = neighbor.insert(stored, tx);
                if (inserted <= 0) continue;
                int extracted = energy.extract(inserted, tx);
                if (extracted == inserted) {
                    tx.commit();
                }
            }
        }
    }

    @Override
    public boolean canWork() {
        if (level == null) return false;
        if (!level.hasNeighborSignal(worldPosition)) return false;
        return energy.getAmountAsInt() < MAX_ENERGY;
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy.deserialize(input);
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

    private final class GeneratorEnergy extends SimpleEnergyHandler {
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

        @Override
        protected void onEnergyChanged(int previousAmount) {
            markDirtyForSave();
        }
    }
}
