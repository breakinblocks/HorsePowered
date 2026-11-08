package com.breakinblocks.horsepowered.compat.create;

import com.breakinblocks.horsepowered.blockentity.VirtualWorker;
import com.breakinblocks.horsepowered.blockentity.WorkerHost;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HorseEngineBlockEntity extends GeneratingKineticBlockEntity implements WorkerHost {

    private final VirtualWorker worker = new VirtualWorker(this);
    private float lastGeneratedSpeed;
    private float lastCapacity;

    public HorseEngineBlockEntity(BlockPos pos, BlockState state) {
        super(CreateCompat.HORSE_ENGINE_BE.get(), pos, state);
    }

    public static float capacityPerRpm(double jumpStrength) {
        return (float) (jumpStrength
                * 10.0
                * HorsePowerConfig.horseEngineStressPerJumpPoint.get()
                / HorsePowerConfig.horseEngineRpm.get());
    }

    public double getJumpStrength() {
        return worker.getJumpStrength();
    }

    public float getStressOutput() {
        return Math.abs(calculateAddedStressCapacity() * getGeneratedSpeed());
    }

    @Override
    public VirtualWorker getVirtualWorker() {
        return worker;
    }

    @Override
    public void markChanged() {
        notifyUpdate();
    }

    @Override
    public boolean canWork() {
        return true;
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
    public float getGeneratedSpeed() {
        return worker.isWorking() ? HorsePowerConfig.horseEngineRpm.get() : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = capacityPerRpm(getJumpStrength());
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            worker.tickClient();
            return;
        }
        worker.tickServer();

        float speed = getGeneratedSpeed();
        float capacity = capacityPerRpm(getJumpStrength());
        if (speed != lastGeneratedSpeed || capacity != lastCapacity) {
            lastGeneratedSpeed = speed;
            lastCapacity = capacity;
            updateGeneratedRotation();
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        worker.save(tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        worker.read(tag, clientPacket);
    }

    @Override
    public void destroy() {
        super.destroy();
        worker.onBlockRemoved();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3, pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }
}
