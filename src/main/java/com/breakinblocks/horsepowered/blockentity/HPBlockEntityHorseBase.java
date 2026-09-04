package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class HPBlockEntityHorseBase extends HPBlockEntityBase implements WorkerHost {

    protected final VirtualWorker worker = new VirtualWorker(this);

    public HPBlockEntityHorseBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public abstract boolean targetReached();

    public abstract int getPositionOffset();

    @Override
    public VirtualWorker getVirtualWorker() {
        return worker;
    }

    @Override
    public void markChanged() {
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        worker.read(tag, level != null && level.isClientSide);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        worker.save(tag);
    }

    public boolean validateArea() {
        return worker.validateArea();
    }

    public void setWorker(PathfinderMob newWorker) {
        worker.setWorker(newWorker);
    }

    public void setWorkerToPlayer(Player player) {
        worker.setWorkerToPlayer(player);
    }

    public void releaseWorkerToWorld() {
        worker.releaseWorkerToWorld();
    }

    public boolean hasWorker() {
        return worker.hasWorker();
    }

    public boolean hasWorkerForDisplay() {
        return worker.hasWorker();
    }

    public String getWorkerDisplayName() {
        return worker.getWorkerDisplayName();
    }

    public boolean isValid() {
        return worker.isValid();
    }

    public void showWorkingAreaHighlight() {
        worker.showWorkingAreaHighlight();
    }

    public boolean shouldShowHighlight() {
        return worker.shouldShowHighlight();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3,
                worldPosition.getX() + 4, worldPosition.getY() + 4, worldPosition.getZ() + 4);
    }

    public static <T extends HPBlockEntityHorseBase> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        blockEntity.tickServer();
    }

    public static <T extends HPBlockEntityHorseBase> void clientTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        blockEntity.tickClient();
    }

    protected void tickClient() {
        worker.tickClient();
    }

    protected void tickServer() {
        worker.tickServer();
    }
}
