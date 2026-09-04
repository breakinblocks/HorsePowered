package com.breakinblocks.horsepowered.compat.create;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blockentity.VirtualWorker;
import com.breakinblocks.horsepowered.blockentity.WorkerHost;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HorseEngineBlockEntity extends GeneratingKineticBlockEntity implements WorkerHost {

    private final VirtualWorker worker = new VirtualWorker(this);
    private double jumpStrength;
    private float lastGeneratedSpeed;
    private float lastCapacity;

    public HorseEngineBlockEntity(BlockPos pos, BlockState state) {
        super(CreateCompat.HORSE_ENGINE_BE.get(), pos, state);
    }

    public static float capacityPerRpm(double jumpStrength) {
        return (float) (jumpStrength * 10.0
                * Configs.horseEngineStressPerJumpPoint.get()
                / Configs.horseEngineRpm.get());
    }

    public static double resolveJumpStrength(PathfinderMob mob) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        Double override = JumpStrengthOverrides.lookup(mob.getType(), id);
        if (override != null) {
            return override;
        }
        if (mob.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
            return mob.getAttributeValue(Attributes.JUMP_STRENGTH);
        }
        return Configs.horseEngineDefaultJumpStrength.get();
    }

    public void onWorkerAttached(PathfinderMob mob) {
        jumpStrength = resolveJumpStrength(mob);
        notifyUpdate();
    }

    public double getJumpStrength() {
        return jumpStrength;
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
        return worker.isWorking() ? Configs.horseEngineRpm.get() : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = capacityPerRpm(jumpStrength);
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
        float capacity = capacityPerRpm(jumpStrength);
        if (speed != lastGeneratedSpeed || capacity != lastCapacity) {
            lastGeneratedSpeed = speed;
            lastCapacity = capacity;
            updateGeneratedRotation();
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        worker.save(tag);
        tag.putDouble("jumpStrength", jumpStrength);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        worker.read(tag, clientPacket);
        jumpStrength = tag.getDouble("jumpStrength");
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null && !level.isClientSide && worker.hasWorker()) {
            worker.releaseWorkerToWorld();
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(),
                    new ItemStack(Items.LEAD));
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3,
                pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }
}
