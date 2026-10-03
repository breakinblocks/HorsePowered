package com.breakinblocks.horsepowered.events;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.util.Utils;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HorsePowerMod.MOD_ID)
public class WorkerLeashEventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LeashFenceKnotEntity knot) {
            onKnotInteract(event, knot);
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.LEAD)) return;
        if (!(event.getTarget() instanceof PathfinderMob mob)) return;
        if (!mob.isAlive() || !isTagLeashedWorker(mob)) return;
        if (mob.getLeashHolder() instanceof Player) return;

        if (event.getLevel().isClientSide()) {
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }

        Player player = event.getEntity();
        if (mob.isLeashed()) {
            mob.dropLeash();
        }
        mob.setLeashedTo(player, true);
        mob.playSound(SoundEvents.LEAD_TIED);
        stack.consume(1, player);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        if (player.isSpectator()) return;
        if (player.isSecondaryUseActive()
                && !(player.getMainHandItem().isEmpty()
                        && player.getOffhandItem().isEmpty())) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(BlockTags.FENCES)) return;

        List<Leashable> leashed =
                Leashable.leashableInArea(level, Vec3.atCenterOf(pos), l -> l.getLeashHolder() == player);
        if (leashed.stream().noneMatch(WorkerLeashEventHandler::isTagLeashedWorker)) return;

        if (level.isClientSide()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        Optional<LeashFenceKnotEntity> existingKnot = LeashFenceKnotEntity.getKnot(level, pos);
        LeashFenceKnotEntity knot = existingKnot.orElseGet(() -> LeashFenceKnotEntity.createKnot(level, pos));
        boolean anyLeashed = false;
        for (Leashable leashable : leashed) {
            if (canAttach(leashable, knot)) {
                leashable.setLeashedTo(knot, true);
                anyLeashed = true;
            }
        }

        if (!anyLeashed) {
            if (existingKnot.isEmpty()) {
                knot.discard();
            }
            return;
        }

        knot.playPlacementSound();
        level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(player));
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }

    private static void onKnotInteract(PlayerInteractEvent.EntityInteract event, LeashFenceKnotEntity knot) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        if (event.getItemStack().canPerformAction(ItemAbilities.SHEARS_HARVEST)) return;

        List<Leashable> fromPlayer = Leashable.leashableLeashedTo(player);
        List<Leashable> onKnot = Leashable.leashableLeashedTo(knot);
        boolean involvesWorker = fromPlayer.stream().anyMatch(WorkerLeashEventHandler::isTagLeashedWorker)
                || (fromPlayer.isEmpty() && onKnot.stream().anyMatch(WorkerLeashEventHandler::isTagLeashedWorker));
        if (!involvesWorker) return;

        if (event.getLevel().isClientSide()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        boolean attached = false;
        for (Leashable leashable : fromPlayer) {
            if (canAttach(leashable, knot)) {
                leashable.setLeashedTo(knot, true);
                attached = true;
            }
        }

        boolean retrieved = false;
        if (!attached && !player.isSecondaryUseActive()) {
            for (Leashable leashable : onKnot) {
                if (canAttach(leashable, player)) {
                    leashable.setLeashedTo(player, true);
                    retrieved = true;
                }
            }
        }

        if (!attached && !retrieved) return;

        knot.gameEvent(GameEvent.BLOCK_ATTACH, player);
        knot.playSound(SoundEvents.LEAD_TIED);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static boolean canAttach(Leashable leashable, Entity holder) {
        if (leashable.canHaveALeashAttachedTo(holder)) return true;
        return isTagLeashedWorker(leashable)
                && leashable != holder
                && leashable.leashDistanceTo(holder) <= leashable.leashSnapDistance();
    }

    private static boolean isTagLeashedWorker(Object entity) {
        return entity instanceof PathfinderMob mob && !mob.canBeLeashed() && Utils.isValidWorker(mob);
    }
}
