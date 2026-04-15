package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

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

        BlockState state = getBlockState();
        if (state.hasProperty(BlockGenerator.POWERED) && state.getValue(BlockGenerator.POWERED) != working) {
            level.setBlock(worldPosition, state.setValue(BlockGenerator.POWERED, working), 3);
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

    private static final class GeneratorEnergy extends SimpleEnergyHandler {
        GeneratorEnergy() {
            super(MAX_ENERGY, 0, FE_PER_TICK);
        }

        void generate(int amount) {
            if (amount <= 0) return;
            this.energy = Math.min(this.capacity, this.energy + amount);
        }
    }
}
