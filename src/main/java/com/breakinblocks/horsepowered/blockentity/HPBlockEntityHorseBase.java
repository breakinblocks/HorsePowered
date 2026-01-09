package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class HPBlockEntityHorseBase extends HPBlockEntityBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HPBlockEntityHorseBase.class);

    // NBT tag key used to mark entities as horse-powered workers (persists with entity like a name tag)
    private static final String WORKER_TAG = HorsePowerMod.MOD_ID + ":worker";

    // 8-point square path around the block (symmetric)
    // Values are multiplied by 2 in getPathPosition(), so 1.5 = 3 blocks from center
    protected static final double[][] PATH = {
            {-1.5, -1.5}, {0, -1.5}, {1.5, -1.5}, {1.5, 0},
            {1.5, 1.5}, {0, 1.5}, {-1.5, 1.5}, {-1.5, 0}
    };

    protected AABB[] searchAreas = new AABB[8];
    protected List<BlockPos> searchPos = null;
    protected int origin = -1;
    protected int target = -1;

    protected boolean hasWorker = false;
    protected PathfinderMob worker;
    // UUID storage (replacing CompoundTag nbtWorker)
    protected long workerUuidMost = 0L;
    protected long workerUuidLeast = 0L;
    protected boolean hasStoredWorkerUuid = false;

    protected boolean valid = false;
    protected int validationTimer = 0;
    protected int locateHorseTimer = 0;
    protected boolean running = true;
    protected boolean wasRunning = false;

    // Grace period after finding worker to prevent false "permanently lost" triggers
    // during world load when entities might not be fully initialized
    protected int workerGracePeriod = 0;
    private static final int WORKER_GRACE_TICKS = 200; // 10 seconds grace period
    private static final int LOCATE_TIMER_FAST = 20;   // During grace period, search every second
    private static final int LOCATE_TIMER_SLOW = 120;  // After grace period, search every 6 seconds

    // Client-side highlight rendering
    protected int highlightTimer = 0;
    public static final int HIGHLIGHT_DURATION = 100; // 5 seconds

    public HPBlockEntityHorseBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // Drop lead if horse was attached
        if (hasWorkerForDisplay() && level != null) {
            Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(Items.LEAD));
        }
        // Parent handles container contents
        super.preRemoveSideEffects(pos, state);
    }

    /**
     * Validates that the area around the block is clear for the horse to walk
     */
    public abstract boolean validateArea();

    /**
     * Called when the horse reaches a target point in the path
     * @return true if progress was made (item processed)
     */
    public abstract boolean targetReached();

    /**
     * Gets the Y offset for the path positions
     */
    public abstract int getPositionOffset();

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        target = input.getIntOr("target", -1);
        origin = input.getIntOr("origin", -1);
        hasWorker = input.getBooleanOr("hasWorker", false);
        valid = input.getBooleanOr("valid", false);

        // Always load the worker UUID if present - hasWorker flag may be stale
        // but we can still try to find the worker by UUID
        workerUuidMost = input.getLongOr("workerUuidMost", 0L);
        workerUuidLeast = input.getLongOr("workerUuidLeast", 0L);
        if (workerUuidMost != 0L || workerUuidLeast != 0L) {
            hasStoredWorkerUuid = true;
            // Set grace period on load to prevent false "worker lost" triggers
            // during world load when entities might not be fully initialized yet
            workerGracePeriod = WORKER_GRACE_TICKS;
            UUID uuid = new UUID(workerUuidMost, workerUuidLeast);
            LOGGER.info("[HorsePowered] loadAdditional at {}: Loaded worker UUID={}, hasWorker={}", worldPosition, uuid, hasWorker);
        } else {
            LOGGER.info("[HorsePowered] loadAdditional at {}: No worker UUID found, hasWorker={}", worldPosition, hasWorker);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("target", target);
        output.putInt("origin", origin);
        output.putBoolean("valid", valid);

        // Update stored UUID from current worker if available
        if (worker != null) {
            workerUuidMost = worker.getUUID().getMostSignificantBits();
            workerUuidLeast = worker.getUUID().getLeastSignificantBits();
            hasStoredWorkerUuid = true;
        }
        // Save hasWorker based on whether we have a stored UUID (more reliable than the flag)
        output.putBoolean("hasWorker", hasStoredWorkerUuid);
        // Always save the worker UUID if we have one (preserves UUID across reloads
        // even when the worker entity reference hasn't been restored yet)
        if (hasStoredWorkerUuid) {
            output.putLong("workerUuidMost", workerUuidMost);
            output.putLong("workerUuidLeast", workerUuidLeast);
            UUID uuid = new UUID(workerUuidMost, workerUuidLeast);
            LOGGER.info("[HorsePowered] saveAdditional at {}: Saving worker UUID={}", worldPosition, uuid);
        } else {
            LOGGER.info("[HorsePowered] saveAdditional at {}: No worker UUID to save", worldPosition);
        }
    }

    /**
     * Attempts to find the worker entity by UUID
     */
    private boolean findWorker() {
        if (!hasStoredWorkerUuid || level == null) return false;

        UUID uuid = new UUID(workerUuidMost, workerUuidLeast);
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();

        // Search in a large radius - entities might spawn offset after world reload,
        // or be pushed by other entities during chunk loading
        AABB searchArea = new AABB(x - 48.0D, y - 16.0D, z - 48.0D, x + 48.0D, y + 16.0D, z + 48.0D);

        // Search all PathfinderMobs by UUID (ignoring tag for reconnection)
        List<PathfinderMob> allCreatures = level.getEntitiesOfClass(PathfinderMob.class, searchArea);

        // Log what entities we found for debugging
        if (allCreatures.isEmpty()) {
            LOGGER.info("[HorsePowered] findWorker at {}: Searching for UUID={}, NO creatures found in area!", worldPosition, uuid);
        } else {
            StringBuilder sb = new StringBuilder();
            for (PathfinderMob c : allCreatures) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(c.getClass().getSimpleName()).append("(").append(c.getUUID().toString().substring(0, 8)).append("...)");
            }
            LOGGER.info("[HorsePowered] findWorker at {}: Searching for UUID={}, found {} creatures: {}", worldPosition, uuid, allCreatures.size(), sb);
        }

        for (PathfinderMob creature : allCreatures) {
            if (creature.getUUID().equals(uuid)) {
                // Found the worker by UUID - reconnect even if tag check fails
                // (the tag might not be loaded yet, or modpack removed the entity from tag)
                LOGGER.info("[HorsePowered] findWorker at {}: FOUND worker {} with UUID={}", worldPosition, creature.getClass().getSimpleName(), uuid);
                setWorker(creature);
                // setWorker already calls markWorkerPersistent which sets setPersistenceRequired
                // and stores our marker in the entity's persistent data
                return true;
            }
        }

        LOGGER.warn("[HorsePowered] findWorker at {}: Worker with UUID={} NOT FOUND!", worldPosition, uuid);
        return false;
    }

    /**
     * Sets a new worker entity to power this block
     */
    public void setWorker(PathfinderMob newWorker) {
        hasWorker = true;
        worker = newWorker;
        // Note: restrictTo was removed in 1.21.11, but we use direct position control
        // in tickServer() anyway, so restriction is not needed
        target = getClosestTarget();

        // Mark the worker as persistent (like a name tag) - this survives world saves
        markWorkerPersistent(worker);

        // Set grace period to prevent false "permanently lost" triggers
        // This is especially important during world load when entities might not be fully initialized
        workerGracePeriod = WORKER_GRACE_TICKS;

        if (worker != null) {
            workerUuidMost = worker.getUUID().getMostSignificantBits();
            workerUuidLeast = worker.getUUID().getLeastSignificantBits();
            hasStoredWorkerUuid = true;
            LOGGER.info("[HorsePowered] setWorker at {}: Worker set to {} with UUID={}", worldPosition, worker.getClass().getSimpleName(), worker.getUUID());
        }
        setChanged();
    }

    /**
     * Marks a worker entity as persistent so it won't despawn (like a name tag).
     * This stores data in the entity's persistent NBT which survives world saves.
     */
    private void markWorkerPersistent(PathfinderMob mob) {
        if (mob == null) return;
        // Set the vanilla persistence flag - prevents despawning
        mob.setPersistenceRequired();

        // CRITICAL: Also set a custom name if the entity doesn't have one.
        // In Minecraft, entities with custom names are ALWAYS saved to disk.
        // setPersistenceRequired() only prevents despawning, it doesn't guarantee disk persistence.
        // This is the same mechanism that name tags use to make entities permanent.
        if (!mob.hasCustomName()) {
            // Use an empty text component - this marks it as "has custom name" for persistence
            // but won't display anything visible to the player
            mob.setCustomName(net.minecraft.network.chat.Component.literal(""));
            mob.setCustomNameVisible(false);
            LOGGER.info("[HorsePowered] markWorkerPersistent: Set empty custom name on {} to ensure disk persistence", mob.getClass().getSimpleName());
        }

        // Also store our own marker in the entity's persistent data
        // This data is saved with the entity and survives world reloads
        CompoundTag persistentData = mob.getPersistentData();
        persistentData.putBoolean(WORKER_TAG, true);
        persistentData.putLong(WORKER_TAG + "_machine_x", worldPosition.getX());
        persistentData.putLong(WORKER_TAG + "_machine_y", worldPosition.getY());
        persistentData.putLong(WORKER_TAG + "_machine_z", worldPosition.getZ());
    }

    /**
     * Removes the persistent worker marker from an entity when it's released.
     */
    private void clearWorkerPersistentMarker(PathfinderMob mob) {
        if (mob == null) return;
        CompoundTag persistentData = mob.getPersistentData();
        persistentData.remove(WORKER_TAG);
        persistentData.remove(WORKER_TAG + "_machine_x");
        persistentData.remove(WORKER_TAG + "_machine_y");
        persistentData.remove(WORKER_TAG + "_machine_z");
        // Note: We don't clear setPersistenceRequired - the entity can keep that
        // (it doesn't hurt, and the player might want it to stay)

        // Clear the empty custom name we set for persistence if it's still empty
        // (don't clear if the player gave it a real name)
        if (mob.hasCustomName()) {
            net.minecraft.network.chat.Component name = mob.getCustomName();
            if (name != null && name.getString().isEmpty()) {
                mob.setCustomName(null);
                LOGGER.info("[HorsePowered] clearWorkerPersistentMarker: Cleared empty custom name from {}", mob.getClass().getSimpleName());
            }
        }
    }

    /**
     * Releases the worker back to a player with a lead
     */
    public void setWorkerToPlayer(Player player) {
        if (hasWorker() && worker.canBeLeashed()) {
            hasWorker = false;
            // Note: clearRestriction was removed in 1.21.11, but we use direct position control
            // so the worker will naturally be free once detached
            // Clear our persistent marker before releasing
            clearWorkerPersistentMarker(worker);
            worker.setLeashedTo(player, true);
            worker = null;
            workerUuidMost = 0L;
            workerUuidLeast = 0L;
            hasStoredWorkerUuid = false;
            setChanged();
        }
    }

    /**
     * Checks if the worker is still valid and working.
     * This method has side effects on the server (drops lead if worker permanently lost).
     * For display purposes only, use hasWorkerForDisplay() instead.
     */
    public boolean hasWorker() {
        // Allow worker to be up to 20 blocks away (400 squared) - entities might load at slightly
        // different positions after world reload
        if (worker != null && worker.isAlive() && !worker.isLeashed() && worker.distanceToSqr(Vec3.atCenterOf(worldPosition)) < 400) {
            return true;
        } else {
            if (worker != null) {
                // Worker entity reference is invalid - but DON'T clear the UUID yet!
                // The entity might just need to be re-found after a world reload.
                // Only drop lead if the worker is confirmed dead or leashed to something else.
                boolean workerPermanentlyLost = !worker.isAlive() || worker.isLeashed();

                LOGGER.debug("[HorsePowered] hasWorker at {}: Worker invalid - alive={}, leashed={}, gracePeriod={}",
                        worldPosition, worker.isAlive(), worker.isLeashed(), workerGracePeriod);

                // Don't consider permanently lost if we're still in the grace period
                // This prevents false triggers during world load when entities might not be fully initialized
                if (workerPermanentlyLost && workerGracePeriod <= 0) {
                    LOGGER.warn("[HorsePowered] hasWorker at {}: Worker PERMANENTLY LOST - dropping lead", worldPosition);
                    // Only drop lead on server side when worker is permanently lost
                    if (level != null && !level.isClientSide()) {
                        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), new ItemStack(Items.LEAD));
                    }
                    // Clear UUID only when permanently lost
                    workerUuidMost = 0L;
                    workerUuidLeast = 0L;
                    hasStoredWorkerUuid = false;
                    // Sync the cleared worker state to clients
                    setChanged();
                    worker = null;
                } else if (workerPermanentlyLost) {
                    // Still in grace period - don't clear anything, just null the reference
                    // We'll try to find the worker again by UUID
                    LOGGER.debug("[HorsePowered] hasWorker at {}: Worker lost but in grace period - will retry", worldPosition);
                    worker = null;
                } else {
                    // Worker is too far but not permanently lost - just null the reference
                    LOGGER.debug("[HorsePowered] hasWorker at {}: Worker too far - clearing reference", worldPosition);
                    worker = null;
                }
            }
            hasWorker = false;
            return false;
        }
    }

    /**
     * Returns whether a worker is attached, without any side effects.
     * Use this for display purposes (Jade, rendering, etc.)
     */
    public boolean hasWorkerForDisplay() {
        // If we have an active worker reference that's valid, return true
        if (worker != null && worker.isAlive() && !worker.isLeashed()) {
            return true;
        }
        // Otherwise, return the stored flag (may be true if we haven't found the entity yet)
        return hasWorker || hasStoredWorkerUuid;
    }

    public PathfinderMob getWorker() {
        return worker;
    }

    public boolean isValid() {
        return valid;
    }

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
     * Returns a list of pairs: BlockPos and boolean (true = clear, false = obstructed)
     */
    public List<Map.Entry<BlockPos, Boolean>> getWorkingAreaPositions() {
        List<Map.Entry<BlockPos, Boolean>> positions = new ArrayList<>();
        if (level == null) return positions;

        // Build positions if not already cached
        if (searchPos == null) {
            validateArea(); // This builds searchPos
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

    /**
     * Gets the world position for a path index.
     * Path is centered on the block's center (not corner).
     */
    private Vec3 getPathPosition(int i) {
        // Add 0.5 to center on the block (block positions are at corners)
        double x = worldPosition.getX() + 0.5 + PATH[i][0] * 2;
        double y = worldPosition.getY() + getPositionOffset();
        double z = worldPosition.getZ() + 0.5 + PATH[i][1] * 2;
        return new Vec3(x, y, z);
    }

    /**
     * Finds the closest path point to the current worker position
     */
    protected int getClosestTarget() {
        if (hasWorker()) {
            double dist = Double.MAX_VALUE;
            int closest = 0;

            for (int i = 0; i < PATH.length; i++) {
                Vec3 pos = getPathPosition(i);
                double tmp = worker.distanceToSqr(pos.x, pos.y, pos.z);
                if (tmp < dist) {
                    dist = tmp;
                    closest = i;
                }
            }

            return closest;
        }
        return 0;
    }

    /**
     * Server tick logic for horse-powered operation
     */
    public static <T extends HPBlockEntityHorseBase> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        blockEntity.tickServer();
    }

    /**
     * Client tick logic (for animations)
     */
    public static <T extends HPBlockEntityHorseBase> void clientTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        blockEntity.tickClient();
    }

    protected void tickClient() {
        // Tick down highlight timer
        if (highlightTimer > 0) {
            highlightTimer--;
        }

        // Try to find worker entity on client for rendering
        if (worker == null && hasStoredWorkerUuid && level != null) {
            findWorkerClient();
        }
    }

    /**
     * Client-side worker finding for rendering purposes.
     * This doesn't set up navigation or persistence, just finds the entity reference.
     */
    private void findWorkerClient() {
        if (!hasStoredWorkerUuid || level == null) return;

        UUID uuid = new UUID(workerUuidMost, workerUuidLeast);
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();

        // Use same search radius as server for consistency
        AABB searchArea = new AABB(x - 48.0D, y - 16.0D, z - 48.0D, x + 48.0D, y + 16.0D, z + 48.0D);
        List<PathfinderMob> creatures = level.getEntitiesOfClass(PathfinderMob.class, searchArea);

        for (PathfinderMob creature : creatures) {
            if (creature.getUUID().equals(uuid)) {
                worker = creature;
                return;
            }
        }
    }

    protected void tickServer() {
        // Decrement grace period (protects against false "worker lost" triggers during world load)
        if (workerGracePeriod > 0) {
            workerGracePeriod--;
            if (workerGracePeriod == 0 && hasStoredWorkerUuid && worker == null) {
                UUID uuid = new UUID(workerUuidMost, workerUuidLeast);
                LOGGER.warn("[HorsePowered] tickServer at {}: Grace period EXPIRED, worker UUID={} still not found!", worldPosition, uuid);
            }
        }

        // Validation timer
        validationTimer--;
        if (validationTimer <= 0) {
            boolean wasValid = valid;
            valid = validateArea();
            validationTimer = valid ? 220 : 60;
            // Sync to client when valid state changes (for Jade tooltip)
            if (wasValid != valid) {
                setChanged();
            }
        }

        boolean flag = false;

        // Try to find worker if we had one but lost reference
        boolean hasWorkerNow = hasWorker();
        if (!hasWorkerNow) {
            locateHorseTimer--;
        }
        if (!hasWorkerNow && hasStoredWorkerUuid && locateHorseTimer <= 0) {
            UUID uuid = new UUID(workerUuidMost, workerUuidLeast);
            LOGGER.info("[HorsePowered] tickServer at {}: Attempting to find worker UUID={}, gracePeriod={}", worldPosition, uuid, workerGracePeriod);
            flag = findWorker();
            if (!flag) {
                LOGGER.info("[HorsePowered] tickServer at {}: Worker NOT FOUND, will retry in {} ticks", worldPosition, workerGracePeriod > 0 ? LOCATE_TIMER_FAST : LOCATE_TIMER_SLOW);
            }
        }
        if (locateHorseTimer <= 0) {
            // Search more frequently during grace period (entities may still be loading)
            locateHorseTimer = workerGracePeriod > 0 ? LOCATE_TIMER_FAST : LOCATE_TIMER_SLOW;
        }

        if (valid) {
            // Check if we should be running
            if (!running && canWork()) {
                running = true;
            } else if (running && !canWork()) {
                running = false;
            }

            if (running != wasRunning) {
                target = getClosestTarget();
                wasRunning = running;
            }

            if (hasWorker()) {
                // Ensure worker stays persistent every tick (belt and suspenders approach)
                // This guarantees the entity won't despawn even if something else clears the flag
                worker.setPersistenceRequired();

                if (running) {
                    Vec3 pathPos = getPathPosition(target);
                    double x = pathPos.x;
                    double y = worker.getY(); // Use worker's Y for collision detection
                    double z = pathPos.z;

                    // Create/update search area for current target using worker's Y level
                    // We recreate it each time since the worker Y might vary slightly
                    searchAreas[target] = new AABB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 1.5D, z + 0.5D);

                    // Check if worker reached the target
                    if (worker.getBoundingBox().intersects(searchAreas[target])) {
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

                    // Stop horse from eating
                    if (worker instanceof AbstractHorse horse && horse.isEating()) {
                        horse.setEating(false);
                    }

                    // Navigate to target - use direct position control for all workers
                    // Navigation systems are unreliable, especially for tamed horses
                    if (target != -1) {
                        pathPos = getPathPosition(target);

                        // Calculate direction to target
                        double dx = pathPos.x - worker.getX();
                        double dz = pathPos.z - worker.getZ();
                        double dist = Math.sqrt(dx * dx + dz * dz);

                        if (dist > 0.5) { // Only move if not already at target
                            // Normalize and calculate step toward target
                            double speed = 0.12; // Movement per tick (about 2.4 blocks/sec)
                            double stepX = (dx / dist) * speed;
                            double stepZ = (dz / dist) * speed;

                            // Directly move the entity position (bypasses all AI)
                            double newX = worker.getX() + stepX;
                            double newZ = worker.getZ() + stepZ;
                            worker.setPos(newX, worker.getY(), newZ);

                            // Make the worker look in the direction of movement
                            float targetYaw = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
                            worker.setYRot(targetYaw);
                            worker.yBodyRot = targetYaw;
                            worker.yHeadRot = targetYaw;
                        }
                    }
                }
            }
        }

        if (flag) {
            setChanged();
        }
    }
}
