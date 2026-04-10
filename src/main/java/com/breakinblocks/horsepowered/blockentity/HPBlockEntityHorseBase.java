package com.breakinblocks.horsepowered.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

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

    // Client-side: cached entity for rendering (not in the world)
    private transient Entity cachedRenderEntity;
    private transient String cachedRenderEntityType;

    // Client-side highlight rendering
    protected int highlightTimer = 0;
    public static final int HIGHLIGHT_DURATION = 100; // 5 seconds

    // Movement speed (blocks per tick)
    private static final double MOVEMENT_SPEED = 0.12;

    public HPBlockEntityHorseBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (hasVirtualWorker && level != null && !level.isClientSide()) {
            // Spawn the stored entity back into the world
            spawnStoredEntity();
            // Drop lead
            Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(Items.LEAD));
        }
        // Parent handles container contents
        super.preRemoveSideEffects(pos, state);
    }

    /**
     * Validates that the area around the block is clear for the horse to walk.
     * Default implementation checks a 7x7 area (excluding center 3x3) at Y=0 and Y=1.
     */
    public boolean validateArea() {
        if (level == null) return false;

        if (searchPos == null) {
            searchPos = Lists.newArrayList();

            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if ((x <= 1 && x >= -1) && (z <= 1 && z >= -1)) {
                        continue;
                    }
                    searchPos.add(worldPosition.offset(x, 0, z));
                    searchPos.add(worldPosition.offset(x, 1, z));
                }
            }
        }

        for (BlockPos pos : searchPos) {
            BlockState state = level.getBlockState(pos);
            if (!state.canBeReplaced()) {
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        target = input.getIntOr("target", -1);
        origin = input.getIntOr("origin", -1);
        valid = input.getBooleanOr("valid", false);
        running = input.getBooleanOr("running", true);

        // Load virtual worker data
        boolean hadWorkerBefore = hasVirtualWorker;
        hasVirtualWorker = input.getBooleanOr("hasVirtualWorker", false);
        if (hasVirtualWorker) {
            input.read("workerEntityData", CompoundTag.CODEC).ifPresent(tag -> workerEntityData = tag);
            workerEntityTypeId = input.getStringOr("workerEntityTypeId", null);
            workerDisplayName = input.getStringOr("workerDisplayName", "Worker");
            workerEntityHeight = input.getFloatOr("workerEntityHeight", 1.4f);

            // Load virtual position
            double loadedX = input.getDoubleOr("virtualX", worldPosition.getX() + 0.5);
            double loadedZ = input.getDoubleOr("virtualZ", worldPosition.getZ() + 0.5);
            float loadedYRot = input.getFloatOr("virtualYRot", 0f);

            if (hadWorkerBefore && level != null && level.isClientSide()) {
                // Incremental client sync: update position but keep the client's
                // smoothly-interpolated rotation to avoid single-tick mirror flips
                virtualX = loadedX;
                virtualZ = loadedZ;
                prevVirtualX = loadedX;
                prevVirtualZ = loadedZ;
                // Don't overwrite virtualYRot / prevVirtualYRot — client computes these
            } else {
                // Initial load (server, or first client sync): use server values
                virtualX = loadedX;
                virtualZ = loadedZ;
                virtualYRot = loadedYRot;
                prevVirtualX = loadedX;
                prevVirtualZ = loadedZ;
                prevVirtualYRot = loadedYRot;
            }

            // Invalidate client render cache on load
            cachedRenderEntity = null;
            cachedRenderEntityType = null;
        } else {
            workerEntityData = null;
            workerEntityTypeId = null;
            workerDisplayName = null;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("target", target);
        output.putInt("origin", origin);
        output.putBoolean("valid", valid);
        output.putBoolean("running", running);

        // Save virtual worker data
        output.putBoolean("hasVirtualWorker", hasVirtualWorker);
        if (hasVirtualWorker) {
            if (workerEntityData != null) {
                output.store("workerEntityData", CompoundTag.CODEC, workerEntityData);
            }
            if (workerEntityTypeId != null) {
                output.putString("workerEntityTypeId", workerEntityTypeId);
            }
            if (workerDisplayName != null) {
                output.putString("workerDisplayName", workerDisplayName);
            }
            output.putFloat("workerEntityHeight", workerEntityHeight);

            // Save virtual position
            output.putDouble("virtualX", virtualX);
            output.putDouble("virtualZ", virtualZ);
            output.putFloat("virtualYRot", virtualYRot);
        }
    }

    // --- Worker management ---

    /**
     * Sets a new worker entity to power this block.
     * Serializes the entity data and removes it from the world.
     */
    public void setWorker(PathfinderMob newWorker) {
        // Serialize entity data using 26.1 ValueOutput API
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, level.registryAccess());
        newWorker.saveAsPassenger(output);
        workerEntityData = output.buildResult();
        workerEntityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(newWorker.getType()).toString();
        workerEntityHeight = newWorker.getBbHeight();
        hasVirtualWorker = true;

        // Store display name - use entity type translation as fallback
        String name = newWorker.getDisplayName().getString();
        workerDisplayName = (name != null && !name.isEmpty()) ? name :
                BuiltInRegistries.ENTITY_TYPE.getKey(newWorker.getType()).getPath();

        // Initialize virtual position at entity's current position
        virtualX = newWorker.getX();
        virtualZ = newWorker.getZ();
        prevVirtualX = virtualX;
        prevVirtualZ = virtualZ;
        virtualYRot = newWorker.getYRot();
        prevVirtualYRot = virtualYRot;

        // Find closest path target from current position
        target = getClosestTarget();

        // Don't start running until the tick loop confirms there's work to do
        running = false;
        wasRunning = false;

        // Remove entity from world
        newWorker.discard();

        LOGGER.info("[HorsePowered] setWorker at {}: Stored {} ({})", worldPosition, workerDisplayName, workerEntityTypeId);
        setChanged();
    }

    /**
     * Releases the worker back to a player with a lead.
     */
    public void setWorkerToPlayer(Player player) {
        if (!hasVirtualWorker || level == null || level.isClientSide()) return;

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
    private void spawnStoredEntity() {
        if (!hasVirtualWorker || level == null || level.isClientSide()) return;

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
            Identifier typeId = Identifier.parse(workerEntityTypeId);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(typeId);
            Entity entity = type.create(level, EntitySpawnReason.LOAD);
            if (entity instanceof PathfinderMob mob) {
                ValueInput input = TagValueInput.create(
                        ProblemReporter.DISCARDING, level.registryAccess(), workerEntityData);
                mob.load(input);
                return mob;
            } else {
                LOGGER.warn("[HorsePowered] recreateEntity: Created entity is not a PathfinderMob: {}", workerEntityTypeId);
                if (entity != null) entity.discard();
            }
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
        if (level == null || !level.isClientSide() || !hasVirtualWorker) return null;

        // Create or refresh cache if entity type changed
        if (cachedRenderEntity == null || !workerEntityTypeId.equals(cachedRenderEntityType)) {
            cachedRenderEntity = null;
            cachedRenderEntityType = null;

            if (workerEntityTypeId == null || workerEntityData == null) return null;

            try {
                Identifier typeId = Identifier.parse(workerEntityTypeId);
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(typeId);
                Entity entity = type.create(level, EntitySpawnReason.LOAD);
                if (entity != null) {
                    ValueInput input = TagValueInput.create(
                            ProblemReporter.DISCARDING, level.registryAccess(), workerEntityData);
                    entity.load(input);
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

    /**
     * Starts showing the working area highlight
     */
    public void showWorkingAreaHighlight() {
        highlightTimer = HIGHLIGHT_DURATION;
    }

    /**
     * Checks if the working area highlight should be rendered
     */
    public boolean shouldShowHighlight() {
        return highlightTimer > 0;
    }

    /**
     * Gets the list of positions that need to be clear for the working area.
     */
    public List<Map.Entry<BlockPos, Boolean>> getWorkingAreaPositions() {
        List<Map.Entry<BlockPos, Boolean>> positions = new ArrayList<>();
        if (level == null) return positions;

        if (searchPos == null) {
            validateArea();
        }

        if (searchPos != null) {
            for (BlockPos pos : searchPos) {
                BlockState state = level.getBlockState(pos);
                boolean isClear = state.canBeReplaced();
                positions.add(Map.entry(pos, isClear));
            }
        }
        return positions;
    }

    // --- Path / position logic ---

    /**
     * Gets the world position for a path index.
     */
    private Vec3 getPathPosition(int i) {
        double x = worldPosition.getX() + 0.5 + PATH[i][0] * 2;
        double y = worldPosition.getY() + getPositionOffset();
        double z = worldPosition.getZ() + 0.5 + PATH[i][1] * 2;
        return new Vec3(x, y, z);
    }

    /**
     * Finds the closest path point to the current virtual position.
     */
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
        // Tick down highlight timer
        if (highlightTimer > 0) {
            highlightTimer--;
        }

        // Animate virtual position on client for smooth rendering
        if (hasVirtualWorker) {
            if (running && valid) {
                moveVirtualPosition();
            }
            updateCachedRenderEntity();
        }
    }

    /**
     * Updates the cached render entity's walk animation.
     * Rotation is handled in VirtualWorkerRenderer right before extractRenderState
     * to avoid double-interpolation artifacts (180° flips).
     */
    private void updateCachedRenderEntity() {
        Entity entity = getCachedRenderEntity();
        if (entity == null) return;

        if (entity instanceof LivingEntity living) {
            // Drive walk animation only when actually moving
            if (running && valid) {
                living.walkAnimation.update(0.6f, 0.4f, 1.0f);
            } else {
                living.walkAnimation.update(0.0f, 0.4f, 1.0f);
            }
        }
    }

    protected void tickServer() {
        // Validation timer
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
            // Check if we should be running
            if (!running && canWork()) {
                running = true;
            } else if (running && !canWork()) {
                running = false;
            }

            if (running != wasRunning) {
                target = getClosestTarget();
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

    /**
     * Moves the virtual position toward the current target path point (server-side).
     * Also handles target reaching and path progression.
     * @return true if progress was made (item processed)
     */
    private boolean moveVirtualPositionServer() {
        if (target < 0 || target >= PATH.length) {
            target = 0;
        }

        boolean flag = false;

        Vec3 pathPos = getPathPosition(target);
        double y = worldPosition.getY() + getPositionOffset();

        // Create/update search area for current target
        searchAreas[target] = new AABB(
                pathPos.x - 0.5, y - 0.5, pathPos.z - 0.5,
                pathPos.x + 0.5, y + 1.5, pathPos.z + 0.5);

        // Check if virtual position reached the target
        double dx = virtualX - pathPos.x;
        double dz = virtualZ - pathPos.z;
        double distSq = dx * dx + dz * dz;

        if (distSq < 0.16) { // 0.4^2 — tighter threshold for denser circle points
            int next = target + 1;
            int previous = target - 1;
            if (next >= PATH.length) next = 0;
            if (previous < 0) previous = PATH.length - 1;

            // Process if we moved to a new position
            if (origin != target && target != previous) {
                origin = target;
                flag = targetReached();
            }
            target = next;
        }

        // Move virtual position toward target
        moveVirtualPosition();

        return flag;
    }

    // How quickly rotation catches up to movement direction (0-1, higher = snappier)
    private static final float ROTATION_SMOOTHING = 0.25f;

    /**
     * Moves the virtual position one step toward the current target.
     * Used by both server and client tick.
     */
    private void moveVirtualPosition() {
        if (target < 0 || target >= PATH.length) return;

        // Save previous for interpolation
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

            // Smoothly rotate toward movement direction to avoid snapping
            float targetYRot = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
            float diff = targetYRot - virtualYRot;
            // Wrap to [-180, 180] to always take the shortest turn
            while (diff < -180) diff += 360;
            while (diff > 180) diff -= 360;
            virtualYRot += diff * ROTATION_SMOOTHING;
        }
    }
}
