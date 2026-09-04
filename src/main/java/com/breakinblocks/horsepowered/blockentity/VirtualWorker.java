package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.Configs;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VirtualWorker {

    public static final int PATH_POINTS = 24;
    public static final double CIRCLE_RADIUS = 1.5;
    private static final double[][] PATH;

    static {
        PATH = new double[PATH_POINTS][2];
        for (int i = 0; i < PATH_POINTS; i++) {
            double angle = 2.0 * Math.PI * i / PATH_POINTS;
            PATH[i][0] = Math.sin(angle) * CIRCLE_RADIUS;
            PATH[i][1] = -Math.cos(angle) * CIRCLE_RADIUS;
        }
    }

    public static final int HIGHLIGHT_DURATION = 100;
    private static final double MOVEMENT_SPEED = 0.12;
    private static final float ROTATION_SMOOTHING = 0.25F;
    private static final int STOP_GRACE_TICKS = 40;

    private final WorkerHost host;

    private double pathSpeedMultiplier = 1.0;
    private List<BlockPos> searchPos = null;
    private List<BlockPos> floorPos = null;
    private int origin = -1;
    private int target = -1;

    private CompoundTag workerEntityData;
    private String workerEntityTypeId;
    private String workerDisplayName;
    private boolean hasVirtualWorker = false;

    private double virtualX;
    private double virtualZ;
    private double prevVirtualX;
    private double prevVirtualZ;
    private float virtualYRot;
    private float prevVirtualYRot;
    private float workerEntityHeight = 1.4F;

    private boolean valid = false;
    private int validationTimer = 0;
    private boolean running = false;
    private boolean wasRunning = false;
    private int stopGraceTimer = 0;

    private Entity cachedRenderEntity;
    private String cachedRenderEntityType;

    private int highlightTimer = 0;

    public VirtualWorker(WorkerHost host) {
        this.host = host;
    }

    private Level level() {
        return host.getLevel();
    }

    private BlockPos pos() {
        return host.getBlockPos();
    }

    public boolean validateArea() {
        Level level = level();
        if (level == null) return false;

        if (searchPos == null) {
            searchPos = Lists.newArrayList();
            floorPos = Lists.newArrayList();
            BlockPos base = pos();
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if ((x <= 1 && x >= -1) && (z <= 1 && z >= -1)) {
                        continue;
                    }
                    searchPos.add(base.offset(x, 0, z));
                    searchPos.add(base.offset(x, 1, z));
                    floorPos.add(base.offset(x, -1, z));
                }
            }
        }

        int obstructions = 0;
        int tolerance = Configs.pathObstructionTolerance.get();
        for (BlockPos pos : searchPos) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof LeverBlock) continue;
            if (!state.canBeReplaced()) {
                obstructions++;
                if (obstructions > tolerance) {
                    return false;
                }
            }
        }
        for (BlockPos pos : floorPos) {
            BlockState state = level.getBlockState(pos);
            if (!state.isFaceSturdy(level, pos, Direction.UP)) {
                return false;
            }
        }
        pathSpeedMultiplier = computePathSpeedMultiplier();
        return true;
    }

    private double computePathSpeedMultiplier() {
        Level level = level();
        if (level == null) return 1.0;
        Set<BlockPos> seen = new HashSet<>();
        double sum = 0;
        int count = 0;
        BlockPos base = pos();
        int floorY = base.getY() - 1;
        for (int i = 0; i < PATH_POINTS; i++) {
            double pathX = base.getX() + 0.5 + PATH[i][0] * 2;
            double pathZ = base.getZ() + 0.5 + PATH[i][1] * 2;
            BlockPos pos = new BlockPos((int) Math.floor(pathX), floorY, (int) Math.floor(pathZ));
            if (!seen.add(pos)) continue;
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
            sum += Configs.getPathSpeedMultiplier(id);
            count++;
        }
        return count > 0 ? sum / count : 1.0;
    }

    public void read(CompoundTag tag, boolean clientPacket) {
        target = tag.contains("target") ? tag.getInt("target") : -1;
        origin = tag.contains("origin") ? tag.getInt("origin") : -1;
        valid = tag.getBoolean("valid");
        running = tag.getBoolean("running");
        pathSpeedMultiplier = tag.contains("pathSpeedMultiplier") ? tag.getDouble("pathSpeedMultiplier") : 1.0;

        boolean hadWorkerBefore = hasVirtualWorker;
        hasVirtualWorker = tag.getBoolean("hasVirtualWorker");
        if (hasVirtualWorker) {
            if (tag.contains("workerEntityData", Tag.TAG_COMPOUND)) {
                workerEntityData = tag.getCompound("workerEntityData");
            }
            if (tag.contains("workerEntityTypeId")) {
                workerEntityTypeId = tag.getString("workerEntityTypeId");
            }
            workerDisplayName = tag.contains("workerDisplayName") ? tag.getString("workerDisplayName") : "Worker";
            workerEntityHeight = tag.contains("workerEntityHeight") ? tag.getFloat("workerEntityHeight") : 1.4F;

            BlockPos base = pos();
            double loadedX = tag.contains("virtualX") ? tag.getDouble("virtualX") : base.getX() + 0.5;
            double loadedZ = tag.contains("virtualZ") ? tag.getDouble("virtualZ") : base.getZ() + 0.5;
            float loadedYRot = tag.contains("virtualYRot") ? tag.getFloat("virtualYRot") : 0F;

            virtualX = loadedX;
            virtualZ = loadedZ;
            prevVirtualX = loadedX;
            prevVirtualZ = loadedZ;
            if (!(hadWorkerBefore && clientPacket)) {
                virtualYRot = loadedYRot;
                prevVirtualYRot = loadedYRot;
            }

            if (cachedRenderEntityType != null && !cachedRenderEntityType.equals(workerEntityTypeId)) {
                cachedRenderEntity = null;
                cachedRenderEntityType = null;
            }
        } else {
            workerEntityData = null;
            workerEntityTypeId = null;
            workerDisplayName = null;
            cachedRenderEntity = null;
            cachedRenderEntityType = null;
        }
    }

    public void save(CompoundTag tag) {
        tag.putInt("target", target);
        tag.putInt("origin", origin);
        tag.putBoolean("valid", valid);
        tag.putBoolean("running", running);
        tag.putDouble("pathSpeedMultiplier", pathSpeedMultiplier);

        tag.putBoolean("hasVirtualWorker", hasVirtualWorker);
        if (hasVirtualWorker) {
            if (workerEntityData != null) {
                tag.put("workerEntityData", workerEntityData);
            }
            if (workerEntityTypeId != null) {
                tag.putString("workerEntityTypeId", workerEntityTypeId);
            }
            if (workerDisplayName != null) {
                tag.putString("workerDisplayName", workerDisplayName);
            }
            tag.putFloat("workerEntityHeight", workerEntityHeight);
            tag.putDouble("virtualX", virtualX);
            tag.putDouble("virtualZ", virtualZ);
            tag.putFloat("virtualYRot", virtualYRot);
        }
    }

    public void setWorker(PathfinderMob newWorker) {
        if (level() == null) return;

        CompoundTag data = new CompoundTag();
        if (!newWorker.saveAsPassenger(data)) return;
        workerEntityData = data;
        workerEntityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(newWorker.getType()).toString();
        workerEntityHeight = newWorker.getBbHeight();
        hasVirtualWorker = true;

        String name = newWorker.getDisplayName().getString();
        workerDisplayName = (name != null && !name.isEmpty()) ? name :
                BuiltInRegistries.ENTITY_TYPE.getKey(newWorker.getType()).getPath();

        virtualX = newWorker.getX();
        virtualZ = newWorker.getZ();
        prevVirtualX = virtualX;
        prevVirtualZ = virtualZ;
        virtualYRot = newWorker.getYRot();
        prevVirtualYRot = virtualYRot;

        target = getClosestTarget();
        running = false;
        wasRunning = false;

        newWorker.discard();
        host.markChanged();
    }

    public void setWorkerToPlayer(Player player) {
        Level level = level();
        if (!hasVirtualWorker || level == null || level.isClientSide) return;

        PathfinderMob mob = recreateEntity();
        if (mob != null) {
            mob.setPos(virtualX, getVirtualY(), virtualZ);
            level.addFreshEntity(mob);
            mob.setLeashedTo(player, true);
        }

        clearVirtualWorker();
        host.markChanged();
    }

    public void releaseWorkerToWorld() {
        Level level = level();
        if (!hasVirtualWorker || level == null || level.isClientSide) return;

        PathfinderMob mob = recreateEntity();
        if (mob != null) {
            mob.setPos(virtualX, getVirtualY(), virtualZ);
            level.addFreshEntity(mob);
        }

        clearVirtualWorker();
    }

    @Nullable
    private PathfinderMob recreateEntity() {
        Level level = level();
        if (workerEntityData == null || workerEntityTypeId == null || level == null) return null;
        CompoundTag fullTag = workerEntityData.copy();
        fullTag.putString("id", workerEntityTypeId);
        return EntityType.create(fullTag, level)
                .filter(e -> e instanceof PathfinderMob)
                .map(e -> (PathfinderMob) e)
                .orElse(null);
    }

    private void clearVirtualWorker() {
        workerEntityData = null;
        workerEntityTypeId = null;
        workerDisplayName = null;
        hasVirtualWorker = false;
        target = -1;
        origin = -1;
        running = false;
        cachedRenderEntity = null;
        cachedRenderEntityType = null;
    }

    public boolean hasWorker() {
        return hasVirtualWorker;
    }

    public String getWorkerDisplayName() {
        return workerDisplayName;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isWorking() {
        return running && valid && hasVirtualWorker;
    }

    public double getVirtualX() { return virtualX; }
    public double getVirtualZ() { return virtualZ; }
    public float getVirtualYRot() { return virtualYRot; }
    public double getPrevVirtualX() { return prevVirtualX; }
    public double getPrevVirtualZ() { return prevVirtualZ; }
    public float getPrevVirtualYRot() { return prevVirtualYRot; }
    public double getVirtualY() { return pos().getY() + host.getPositionOffset(); }
    public float getWorkerEntityHeight() { return workerEntityHeight; }

    @Nullable
    public Entity getCachedRenderEntity() {
        Level level = level();
        if (level == null || !level.isClientSide || !hasVirtualWorker) return null;

        if (cachedRenderEntity == null || !workerEntityTypeId.equals(cachedRenderEntityType)) {
            cachedRenderEntity = null;
            cachedRenderEntityType = null;

            if (workerEntityTypeId == null || workerEntityData == null) return null;

            CompoundTag fullTag = workerEntityData.copy();
            fullTag.putString("id", workerEntityTypeId);
            Entity entity = EntityType.create(fullTag, level).orElse(null);
            if (entity != null) {
                entity.setNoGravity(true);
                entity.setSilent(true);
                if (entity instanceof PathfinderMob mob) {
                    mob.setNoAi(true);
                }
                cachedRenderEntity = entity;
                cachedRenderEntityType = workerEntityTypeId;
            }
        }

        return cachedRenderEntity;
    }

    public void showWorkingAreaHighlight() {
        highlightTimer = HIGHLIGHT_DURATION;
    }

    public boolean shouldShowHighlight() {
        return highlightTimer > 0;
    }

    public List<Map.Entry<BlockPos, Boolean>> getWorkingAreaPositions() {
        List<Map.Entry<BlockPos, Boolean>> positions = new ArrayList<>();
        Level level = level();
        if (level == null) return positions;

        if (searchPos == null) {
            validateArea();
        }

        if (searchPos != null) {
            for (BlockPos pos : searchPos) {
                BlockState state = level.getBlockState(pos);
                boolean isClear = state.canBeReplaced() || state.getBlock() instanceof LeverBlock;
                positions.add(Map.entry(pos, isClear));
            }
        }
        if (floorPos != null) {
            for (BlockPos pos : floorPos) {
                BlockState state = level.getBlockState(pos);
                if (!state.isFaceSturdy(level, pos, Direction.UP)) {
                    positions.add(Map.entry(pos, false));
                }
            }
        }
        return positions;
    }

    private Vec3 getPathPosition(int i) {
        BlockPos base = pos();
        double x = base.getX() + 0.5 + PATH[i][0] * 2;
        double y = base.getY() + host.getPositionOffset();
        double z = base.getZ() + 0.5 + PATH[i][1] * 2;
        return new Vec3(x, y, z);
    }

    private int getClosestTarget() {
        if (!hasVirtualWorker) return 0;

        double dist = Double.MAX_VALUE;
        int closest = 0;
        for (int i = 0; i < PATH.length; i++) {
            Vec3 pos = getPathPosition(i);
            double dx = virtualX - pos.x;
            double dz = virtualZ - pos.z;
            double tmp = dx * dx + dz * dz;
            if (tmp < dist) {
                dist = tmp;
                closest = i;
            }
        }
        return closest;
    }

    public void tickClient() {
        if (highlightTimer > 0) {
            highlightTimer--;
        }

        if (hasVirtualWorker) {
            if (running && valid) {
                moveVirtualPosition();
            }
            updateCachedRenderEntity();
        }
    }

    private void updateCachedRenderEntity() {
        Entity entity = getCachedRenderEntity();
        if (entity == null) return;

        entity.tickCount++;

        if (entity instanceof LivingEntity living) {
            if (running && valid) {
                living.walkAnimation.update(0.6F, 0.4F);
            } else {
                living.walkAnimation.update(0.0F, 1.0F);
            }
        }
    }

    public void tickServer() {
        validationTimer--;
        if (validationTimer <= 0) {
            boolean wasValid = valid;
            double previousMultiplier = pathSpeedMultiplier;
            valid = validateArea();
            validationTimer = valid ? 220 : 60;
            if (wasValid != valid || Math.abs(previousMultiplier - pathSpeedMultiplier) > 1.0e-6) {
                host.markChanged();
            }
        }

        boolean flag = false;

        if (valid && hasVirtualWorker) {
            if (host.canWork()) {
                running = true;
                stopGraceTimer = STOP_GRACE_TICKS;
            } else if (running && stopGraceTimer > 0) {
                stopGraceTimer--;
            } else {
                running = false;
            }

            if (running != wasRunning) {
                if (!wasRunning) {
                    target = getClosestTarget();
                }
                wasRunning = running;
                host.markChanged();
            }

            if (running) {
                flag = moveVirtualPositionServer();
            }
        }

        if (flag) {
            host.markChanged();
        }
    }

    private boolean moveVirtualPositionServer() {
        if (target < 0 || target >= PATH.length) {
            target = 0;
        }

        boolean flag = false;

        Vec3 pathPos = getPathPosition(target);

        double dx = virtualX - pathPos.x;
        double dz = virtualZ - pathPos.z;
        double distSq = dx * dx + dz * dz;

        if (distSq < 0.16) {
            int next = target + 1;
            int previous = target - 1;
            if (next >= PATH.length) next = 0;
            if (previous < 0) previous = PATH.length - 1;

            if (origin != target && target != previous) {
                origin = target;
                if (host.canWork()) {
                    flag = host.targetReached();
                }
            }
            target = next;
        }

        moveVirtualPosition();

        return flag;
    }

    private void moveVirtualPosition() {
        if (target < 0 || target >= PATH.length) return;

        prevVirtualX = virtualX;
        prevVirtualZ = virtualZ;
        prevVirtualYRot = virtualYRot;

        Vec3 pathPos = getPathPosition(target);

        double dx = pathPos.x - virtualX;
        double dz = pathPos.z - virtualZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 0.1) {
            double speed = MOVEMENT_SPEED * pathSpeedMultiplier;
            virtualX += (dx / dist) * speed;
            virtualZ += (dz / dist) * speed;

            float targetYRot = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
            float diff = targetYRot - virtualYRot;
            while (diff < -180) diff += 360;
            while (diff > 180) diff -= 360;
            virtualYRot += diff * ROTATION_SMOOTHING;
        }
    }
}
