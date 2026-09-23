package com.breakinblocks.horsepowered.events;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.util.Utils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HorsePowerMod.MOD_ID)
public class WorkerLeashEventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.LEAD)) return;
        if (!(event.getTarget() instanceof PathfinderMob mob)) return;
        if (!mob.isAlive() || mob.canBeLeashed() || !Utils.isValidWorker(mob)) return;
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
}
