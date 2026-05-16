package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class CreativeBatteryBlockEntity extends BlockEntity {

    private final SimpleEnergyHandler energy =
            new SimpleEnergyHandler(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_BATTERY.get(), pos, state);
    }

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity be) {
        if (be.energy.getAmountAsInt() <= 0) return;

        for (Direction dir : Direction.values()) {
            int stored = be.energy.getAmountAsInt();
            if (stored <= 0) return;

            EnergyHandler neighbor = level.getCapability(
                    Capabilities.Energy.BLOCK,
                    pos.relative(dir),
                    dir.getOpposite());
            if (neighbor == null) continue;

            try (Transaction tx = Transaction.openRoot()) {
                int inserted = neighbor.insert(stored, tx);
                if (inserted <= 0) continue;
                int extracted = be.energy.extract(inserted, tx);
                if (extracted == inserted) {
                    tx.commit();
                }
            }
        }
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
}
