package com.breakinblocks.horsepowered.blockentity;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class HPBlockEntityHorseBase extends HPBlockEntityBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HPBlockEntityHorseBase.class);

    // Circular path around the block - 24 evenly spaced points at radius 1.5
    // Values are multiplied by 2 in getPathPosition(), giving a 3-block radius circle
    protected static final int PATH_POINTS = 24;
    protected static final double CIRCLE_RADIUS = 1.5;
    protected static final double[][] PATH;

    static {
        PATH = new double[PATH_POINTS][2];
        for (int i = 0; i < PATH_POINTS; i++) {
            double angle = 2.0 * Math.PI * i / PATH_POINTS;
            PATH[i][0] = Math.sin(angle) * CIRCLE_RADIUS;
            PATH[i][1] = -Math.cos(angle) * CIRCLE_RADIUS;
        }
    }

    protected AABB[] searchAreas = new AABB[PATH_POINTS];
    protected List<BlockPos> searchPos = null;
    protected List<BlockPos> floorPos = null;
    protected int origin = -1;
    protected int target = -1;

    // --- Virtual worker data (replaces live entity) ---
    protected CompoundTag workerEntityData;
    protected String workerEntityTypeId;
    protected String workerDisplayName;
    protected boolean hasVirtualWorker = false;

    // --- Virtual position (computed each tick on both server and client) ---
    protected double virtualX, virtualZ;
    protected double prevVirtualX, prevVirtualZ;
    protected float virtualYRot, prevVirtualYRot;
    // Estimated entity height for leash attachment point calculation
    protected float workerEntityHeight = 1.4f;

    // --- Movement state ---
    protected boolean valid = false;
    protected int validationTimer = 0;
    protected boolean running = false;
    protected boolean wasRunning = false;
    protected int stopGraceTimer = 0;
    protected static final int STOP_GRACE_TICKS = 40;

    // Client-side: cached entity for rendering (not in the world)
    private transient Entity cachedRenderEntity;
    private transient String cachedRenderEntityType;

    // Client-side highlight rendering
    protected int highlightTimer = 0;
    public static final int HIGHLIGHT_DURATION = 100; // 5 seconds

    // Movement speed (blocks per tick)
    private static final double MOVEMENT_SPEED = 0.12;

    // How quickly rotation catches up to movement direction (0-1, higher = snappier)
    private static final float ROTATION_SMOOTHING = 0.25f;

    public HPBlockEntityHorseBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    /**
     * Validates that the area around the block is clear for the horse to walk.
     * Default implementation checks a 7x7 area (excluding center 3x3) at Y=0 and Y=1,
     * plus a sturdy floor at Y=-1. Levers are allowed inside the ring so the area
     * can host redstone toggles for the block.
     */
    public boolean validateArea() {
        if (level == null) return false;

        if (searchPos == null) {
            searchPos = Lists.newArrayList();
            floorPos = Lists.newArrayList();
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if ((x <= 1 && x >= -1) && (z <= 1 && z >= -1)) {
                        continue;
                    }
                    searchPos.add(worldPosition.offset(x, 0, z));
                    searchPos.add(worldPosition.offset(x, 1, z));
                    floorPos.add(worldPosition.offset(x, -1, z));
                }
            }
        }

        for (BlockPos pos : searchPos) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof LeverBlock) continue;
            if (!state.canBeReplaced()) {
                return false;
            }
        }
        for (BlockPos pos : floorPos) {
            BlockState state = level.getBlockState(pos);
            if (!state.isFaceSturdy(level, pos, Direction.UP)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called when the horse reaches a target point in the path
     * @return true if progress was made (item processed)
     */
    public abstract boolean targetReached();

    /**
     * Gets the Y offset for the path positions
     */
    public abstract int getPositionOffset();

    // --- Serialization ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        target = tag.getInt("target");
        origin = tag.getInt("origin");
        valid = tag.getBoolean("valid");
        running = tag.getBoolean("running");

        boolean hadWorkerBefore = hasVirtualWorker;
        hasVirtualWorker = tag.getBoolean("hasVirtualWorker");
        if (hasVirtualWorker) {
            if (tag.contains("workerEntityData")) {
                workerEntityData = tag.getCompound("workerEntityData");
            }
            if (tag.contains("workerEntityTypeId")) {
                workerEntityTypeId = tag.getString("workerEntityTypeId");
            }
            workerDisplayName = tag.contains("workerDisplayName") ? tag.getString("workerDisplayName") : "Worker";
            workerEntityHeight = tag.contains("workerEntityHeight") ? tag.getFloat("workerEntityHeight") : 1.4f;

            double loadedX = tag.contains("virtualX") ? tag.getDouble("virtualX") : worldPosition.getX() + 0.5;
            double loadedZ = tag.contains("virtualZ") ? tag.getDouble("virtualZ") : worldPosition.getZ() + 0.5;
            float loadedYRot = tag.contains("virtualYRot") ? tag.getFloat("virtualYRot") : 0f;

            if (hadWorkerBefore && level != null && level.isClientSide) {
                // Incremental client sync: update position but keep the client's
                // smoothly-interpolated rotation to avoid single-tick mirror flips
                virtualX = loadedX;
                virtualZ = loadedZ;
                prevVirtualX = loadedX;
                prevVirtualZ = loadedZ;
            } else {
                virtualX = loadedX;
                virtualZ = loadedZ;
                virtualYRot = loadedYRot;
                prevVirtualX = loadedX;
                prevVirtualZ = loadedZ;
                prevVirtualYRot = loadedYRot;
            }

            // Only invalidate the cached render entity when the worker identity changed.
            // Resetting it on every position sync would restart the walk animation each tick.
            if (cachedRenderEntityType != null && !cachedRenderEntityType.equals(workerEntityTypeId)) {
                cachedRenderEntity = null;
                cachedRenderEntityType = null;
            }
        } else {
            workerEntityData = null;
            workerEntityTypeId = null;
            workerDisplayName = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("target", target);
        tag.putInt("origin", origin);
        tag.putBoolean("valid", valid);
        tag.putBoolean("running", running);

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

    // --- Worker management ---

    /**
     * Sets a new worker entity to power this block.
     * Serializes the entity data and removes it from the world.
     */
    public void setWorker(PathfinderMob newWorker) {
        if (level == null) return;

        // Serialize entity data (1.21.1 CompoundTag API)
        CompoundTag data = new CompoundTag();
        newWorker.saveAsPassenger(data);
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

        LOGGER.info("[HorsePowered] setWorker at {}: Stored {} ({})", worldPosition, workerDisplayName, workerEntityTypeId);
        setChanged();
    }

    /**
     * Releases the worker back to a player with a lead.
     */
    public void setWorkerToPlayer(Player player) {
        if (!hasVirtualWorker || level == null || level.isClientSide) return;

        PathfinderMob mob = recreateEntity();
        if (mob != null) {
            mob.setPos(virtualX, worldPosition.getY() + getPositionOffset(), virtualZ);
            level.addFreshEntity(mob);
            mob.setLeashedTo(player, true);
            LOGGER.info("[HorsePowered] setWorkerToPlayer at {}: Released {} to player", worldPosition, workerDisplayName);
        }

        clearVirtualWorker();
        setChanged();
    }

    /**
     * Spawns the stored entity back into the world (e.g., when block is broken).
     */
    public void spawnStoredEntity() {
        if (!hasVirtualWorker || level == null || level.isClientSide) return;

        PathfinderMob mob = recreateEntity();
        if (mob != null) {
            mob.setPos(virtualX, worldPosition.getY() + getPositionOffset(), virtualZ);
            level.addFreshEntity(mob);
            LOGGER.info("[HorsePowered] spawnStoredEntity at {}: Spawned {} back into world", worldPosition, workerDisplayName);
        }

        clearVirtualWorker();
    }

    /**
     * Recreates a PathfinderMob entity from the stored data.
     * Returns null if the entity type is invalid or creation fails.
     */
    private PathfinderMob recreateEntity() {
        if (workerEntityData == null || workerEntityTypeId == null || level == null) return null;

        try {
            // In 1.21.1, EntityType.create(CompoundTag, Level) is the preferred way
            // as it reads the entity ID from the tag and handles full NBT load.
            CompoundTag fullTag = workerEntityData.copy();
            fullTag.putString("id", workerEntityTypeId);
            return EntityType.create(fullTag, level)
                    .filter(e -> e instanceof PathfinderMob)
                    .map(e -> (PathfinderMob) e)
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("[HorsePowered] recreateEntity: Failed to recreate entity of type {}", workerEntityTypeId, e);
        }
        return null;
    }

    /**
     * Clears all virtual worker data.
     */
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

    /**
     * Handles block removal: spawn the stored worker back into the world
     * and drop a lead item. Called from the block's onRemove method.
     */
    public void onBlockRemoved() {
        if (hasVirtualWorker && level != null && !level.isClientSide) {
            spawnStoredEntity();
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), new ItemStack(Items.LEAD));
        }
    }

    /**
     * Checks if a virtual worker is attached.
     */
    public boolean hasWorker() {
        return hasVirtualWorker;
    }

    /**
     * Returns whether a worker is attached, for display purposes.
     */
    public boolean hasWorkerForDisplay() {
        return hasVirtualWorker;
    }

    /**
     * Gets the stored worker's display name (for Jade tooltips, etc.)
     */
    public String getWorkerDisplayName() {
        return workerDisplayName;
    }

    public boolean isValid() {
        return valid;
    }

    // --- Virtual position accessors (for renderer) ---

    public double getVirtualX() { return virtualX; }
    public double getVirtualZ() { return virtualZ; }
    public float getVirtualYRot() { return virtualYRot; }
    public double getPrevVirtualX() { return prevVirtualX; }
    public double getPrevVirtualZ() { return prevVirtualZ; }
    public float getPrevVirtualYRot() { return prevVirtualYRot; }
    public double getVirtualY() { return worldPosition.getY() + getPositionOffset(); }
    public float getWorkerEntityHeight() { return workerEntityHeight; }
    public String getWorkerEntityTypeId() { return workerEntityTypeId; }
    public CompoundTag getWorkerEntityData() { return workerEntityData; }

    /**
     * Gets or creates a cached entity for client-side rendering.
     * The entity is NOT in the world - it exists only as a render reference.
     */
    public Entity getCachedRenderEntity() {
        if (level == null || !level.isClientSide || !hasVirtualWorker) return null;

        if (cachedRenderEntity == null || !workerEntityTypeId.equals(cachedRenderEntityType)) {
            cachedRenderEntity = null;
            cachedRenderEntityType = null;

            if (workerEntityTypeId == null || workerEntityData == null) return null;

            try {
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
            } catch (Exception e) {
                LOGGER.warn("[HorsePowered] Failed to create cached render entity: {}", workerEntityTypeId, e);
            }
        }

        return cachedRenderEntity;
    }

    // --- Highlight rendering ---

    public void showWorkingAreaHighlight() {
        highlightTimer = HIGHLIGHT_DURATION;
    }

    public boolean shouldShowHighlight() {
        return highlightTimer > 0;
    }

    public List<Map.Entry<BlockPos, Boolean>> getWorkingAreaPositions() {
        List<Map.Entry<BlockPos, Boolean>> positions = new ArrayList<>();
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

    // --- Path / position logic ---

    private Vec3 getPathPosition(int i) {
        double x = worldPosition.getX() + 0.5 + PATH[i][0] * 2;
        double y = worldPosition.getY() + getPositionOffset();
        double z = worldPosition.getZ() + 0.5 + PATH[i][1] * 2;
        return new Vec3(x, y, z);
    }

    protected int getClosestTarget() {
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

    // --- Tick logic ---

    public static <T extends HPBlockEntityHorseBase> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        blockEntity.tickServer();
    }

    public static <T extends HPBlockEntityHorseBase> void clientTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        blockEntity.tickClient();
    }

    protected void tickClient() {
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
                living.walkAnimation.update(0.6f, 0.4f);
            } else {
                living.walkAnimation.update(0.0f, 1.0f);
            }
        }
    }

    protected void tickServer() {
        validationTimer--;
        if (validationTimer <= 0) {
            boolean wasValid = valid;
            valid = validateArea();
            validationTimer = valid ? 220 : 60;
            if (wasValid != valid) {
                setChanged();
            }
        }

        boolean flag = false;

        if (valid && hasVirtualWorker) {
            if (canWork()) {
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
                setChanged();
            }

            if (running) {
                flag = moveVirtualPositionServer();
            }
        }

        if (flag) {
            setChanged();
        }
    }

    private boolean moveVirtualPositionServer() {
        if (target < 0 || target >= PATH.length) {
            target = 0;
        }

        boolean flag = false;

        Vec3 pathPos = getPathPosition(target);
        double y = worldPosition.getY() + getPositionOffset();

        searchAreas[target] = new AABB(
                pathPos.x - 0.5, y - 0.5, pathPos.z - 0.5,
                pathPos.x + 0.5, y + 1.5, pathPos.z + 0.5);

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
                if (canWork()) {
                    flag = targetReached();
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
            double stepX = (dx / dist) * MOVEMENT_SPEED;
            double stepZ = (dz / dist) * MOVEMENT_SPEED;

            virtualX += stepX;
            virtualZ += stepZ;

            float targetYRot = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
            float diff = targetYRot - virtualYRot;
            while (diff < -180) diff += 360;
            while (diff > 180) diff -= 360;
            virtualYRot += diff * ROTATION_SMOOTHING;
        }
    }
}
