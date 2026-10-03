package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class CreativeBatteryBlockEntity extends BlockEntity {

    private static final EnergyHandler INFINITE = new EnergyHandler() {
        @Override
        public long getAmountAsLong() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getCapacityAsLong() {
            return Long.MAX_VALUE;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return Math.max(amount, 0);
        }
    };

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_BATTERY.get(), pos, state);
    }

    public EnergyHandler getEnergyHandler() {
        return INFINITE;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity be) {
        for (Direction dir : Direction.values()) {
            EnergyHandler neighbor =
                    level.getCapability(Capabilities.Energy.BLOCK, pos.relative(dir), dir.getOpposite());
            if (neighbor == null || neighbor == INFINITE) continue;

            try (Transaction tx = Transaction.openRoot()) {
                if (neighbor.insert(Integer.MAX_VALUE, tx) > 0) {
                    tx.commit();
                }
            }
        }
    }
}
