package com.breakinblocks.horsepowered.events;

import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.util.Utils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class WorkerLeashEventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.LEAD)) return;
        if (!(event.getTarget() instanceof PathfinderMob mob)) return;

        Player player = event.getEntity();
        if (!mob.isAlive() || mob.isLeashed() || mob.canBeLeashed(player) || !Utils.isValidWorker(mob)) return;

        Level level = event.getLevel();
        if (!level.isClientSide) {
            mob.setLeashedTo(player, true);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
