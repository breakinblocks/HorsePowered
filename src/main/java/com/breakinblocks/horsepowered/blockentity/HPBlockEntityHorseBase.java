package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class HPBlockEntityHorseBase extends HPBlockEntityBase {

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
    protected CompoundTag nbtWorker;

    protected boolean valid = false;
    protected int validationTimer = 0;
    protected int locateHorseTimer = 0;
    protected boolean running = true;
    protected boolean wasRunning = false;

    // Client-side highlight rendering
    protected int highlightTimer = 0;
    public static final int HIGHLIGHT_DURATION = 100; // 5 seconds

    public HPBlockEntityHorseBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
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
    public void load(CompoundTag tag) {
        super.load(tag);

        target = tag.getInt("target");
        origin = tag.getInt("origin");
        hasWorker = tag.getBoolean("hasWorker");
        valid = tag.getBoolean("valid");

        // Always load the worker UUID if present - hasWorker flag may be stale
        // but we can still try to find the worker by UUID
        if (tag.contains("leash")) {
            nbtWorker = tag.getCompound("leash");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("target", target);
        tag.putInt("origin", origin);
        tag.putBoolean("hasWorker", hasWorker);
        tag.putBoolean("valid", valid);

        // Update nbtWorker from current worker if available
        if (worker != null) {
            if (nbtWorker == null) {
                CompoundTag workerTag = new CompoundTag();
                workerTag.putUUID("UUID", worker.getUUID());
                nbtWorker = workerTag;
            }
        }
        // Always save the leash tag if we have worker data (preserves UUID across reloads
        // even when the worker entity reference hasn't been restored yet)
        if (nbtWorker != null) {
            tag.put("leash", nbtWorker);
        }
    }

    /**
     * Attempts to find the worker entity by UUID
     */
    private boolean findWorker() {
        if (nbtWorker == null || level == null) return false;

        UUID uuid = nbtWorker.getUUID("UUID");
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();

        AABB searchArea = new AABB(x - 7.0D, y - 7.0D, z - 7.0D, x + 7.0D, y + 7.0D, z + 7.0D);

        // Search all PathfinderMobs by UUID (ignoring tag for reconnection)
        List<PathfinderMob> allCreatures = level.getEntitiesOfClass(PathfinderMob.class, searchArea);

        for (PathfinderMob creature : allCreatures) {
            if (creature.getUUID().equals(uuid)) {
                // Found the worker by UUID - reconnect even if tag check fails
                // (the tag might not be loaded yet, or modpack removed the entity from tag)
                setWorker(creature);
                creature.setPersistenceRequired();
                return true;
            }
        }

        return false;
    }

    /**
     * Sets a new worker entity to power this block
     */
    public void setWorker(PathfinderMob newWorker) {
        hasWorker = true;
        worker = newWorker;
        // Path positions can be up to ~4.24 blocks from center (corners at -3,-3)
        // Use radius of 5 to allow navigation to all path points
        worker.restrictTo(worldPosition, 5);
        // Prevent the worker from despawning naturally while attached
        // Note: PathfinderMob extends Mob, so we can call this directly
        worker.setPersistenceRequired();
        target = getClosestTarget();

        if (worker != null) {
            CompoundTag workerTag = new CompoundTag();
            workerTag.putUUID("UUID", worker.getUUID());
            nbtWorker = workerTag;
        }
        setChanged();
    }

    /**
     * Releases the worker back to a player with a lead
     */
    public void setWorkerToPlayer(Player player) {
        if (hasWorker() && worker.canBeLeashed(player)) {
            hasWorker = false;
            worker.clearRestriction();
            worker.setLeashedTo(player, true);
            worker = null;
            nbtWorker = null;
            setChanged();
        }
    }

    /**
     * Checks if the worker is still valid and working.
     * This method has side effects on the server (drops lead if worker lost).
     * For display purposes only, use hasWorkerForDisplay() instead.
     */
    public boolean hasWorker() {
        if (worker != null && worker.isAlive() && !worker.isLeashed() && worker.distanceToSqr(Vec3.atCenterOf(worldPosition)) < 45) {
            return true;
        } else {
            if (worker != null) {
                // Only drop lead on server side
                if (level != null && !level.isClientSide) {
                    Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), new ItemStack(Items.LEAD));
                }
                worker = null;
                nbtWorker = null;
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
        return hasWorker || nbtWorker != null;
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
    public List<java.util.Map.Entry<BlockPos, Boolean>> getWorkingAreaPositions() {
        List<java.util.Map.Entry<BlockPos, Boolean>> positions = new ArrayList<>();
        if (level == null) return positions;

        // Build positions if not already cached
        if (searchPos == null) {
            validateArea(); // This builds searchPos
        }

        if (searchPos != null) {
            for (BlockPos pos : searchPos) {
                BlockState state = level.getBlockState(pos);
                boolean isClear = state.canBeReplaced();
                positions.add(java.util.Map.entry(pos, isClear));
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
        if (worker == null && nbtWorker != null && level != null) {
            findWorkerClient();
        }
    }

    /**
     * Client-side worker finding for rendering purposes.
     * This doesn't set up navigation or persistence, just finds the entity reference.
     */
    private void findWorkerClient() {
        if (nbtWorker == null || level == null || !nbtWorker.contains("UUID")) return;

        UUID uuid = nbtWorker.getUUID("UUID");
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();

        AABB searchArea = new AABB(x - 10.0D, y - 10.0D, z - 10.0D, x + 10.0D, y + 10.0D, z + 10.0D);
        List<PathfinderMob> creatures = level.getEntitiesOfClass(PathfinderMob.class, searchArea);

        for (PathfinderMob creature : creatures) {
            if (creature.getUUID().equals(uuid)) {
                worker = creature;
                return;
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
        if (!hasWorkerNow && nbtWorker != null && locateHorseTimer <= 0) {
            flag = findWorker();
        }
        if (locateHorseTimer <= 0) {
            locateHorseTimer = 120;
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
