package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.VirtualWorker;
import com.breakinblocks.horsepowered.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.BiConsumer;

public final class WorkerInteraction {

    private WorkerInteraction() {
    }

    public static boolean tryAttachLeashed(Level level, BlockPos pos, Player player, VirtualWorker worker,
                                           BiConsumer<Player, PathfinderMob> onAttached) {
        if (worker.hasWorker()) return false;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        List<PathfinderMob> creatures = Utils.getValidCreatures(level,
                new AABB(x - 7.0D, y - 7.0D, z - 7.0D, x + 7.0D, y + 7.0D, z + 7.0D));

        for (PathfinderMob mob : creatures) {
            if (mob.isLeashed() && mob.getLeashHolder() == player) {
                if (!level.isClientSide) {
                    mob.dropLeash(true, false);
                    worker.setWorker(mob);
                    onAttached.accept(player, mob);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean tryRelease(Level level, Player player, VirtualWorker worker) {
        if (!worker.hasWorker()) return false;
        if (!level.isClientSide) {
            worker.setWorkerToPlayer(player);
        }
        return true;
    }

    public static void showHighlight(Level level, VirtualWorker worker) {
        if (level.isClientSide) {
            worker.showWorkingAreaHighlight();
        }
    }
}
