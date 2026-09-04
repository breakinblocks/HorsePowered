package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface WorkerHost {

    Level getLevel();

    BlockPos getBlockPos();

    void markChanged();

    boolean canWork();

    boolean targetReached();

    int getPositionOffset();

    VirtualWorker getVirtualWorker();
}
