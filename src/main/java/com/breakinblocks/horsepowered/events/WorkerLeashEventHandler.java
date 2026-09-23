package com.breakinblocks.horsepowered.events;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.util.Utils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
        if (!mob.isAlive() || mob.isLeashed() || mob.canBeLeashed() || !Utils.isValidWorker(mob)) return;

        Level level = event.getLevel();
        Player player = event.getEntity();
        if (!level.isClientSide) {
            mob.setLeashedTo(player, true);
        }
        stack.consume(1, player);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
