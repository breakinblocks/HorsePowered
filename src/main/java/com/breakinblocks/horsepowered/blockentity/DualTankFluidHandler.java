package com.breakinblocks.horsepowered.blockentity;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes an internal two-index {@link FluidStacksResourceHandler} as a directional
 * fluid handler: external insertions go only to the input tank (index 0), and
 * external extractions drain only the output tank (index 1).
 *
 * <p>Internal code can still bypass the restrictions by talking to the underlying
 * tanks directly via {@link #getStorage()}.
 */
public class DualTankFluidHandler implements ResourceHandler<FluidResource> {

    public static final int INPUT_INDEX = 0;
    public static final int OUTPUT_INDEX = 1;

    private final FluidStacksResourceHandler storage;

    public DualTankFluidHandler(FluidStacksResourceHandler storage) {
        if (storage.size() != 2) {
            throw new IllegalArgumentException("DualTankFluidHandler expects exactly 2 indices, got " + storage.size());
        }
        this.storage = storage;
    }

    public FluidStacksResourceHandler getStorage() {
        return storage;
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return storage.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return storage.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return storage.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        // Only the input tank should advertise accepting new fluid — the output tank
        // is reserved for recipe results.
        return index == INPUT_INDEX && storage.isValid(index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (index != INPUT_INDEX) return 0;
        return storage.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (index != OUTPUT_INDEX) return 0;
        return storage.extract(index, resource, amount, transaction);
    }
}
