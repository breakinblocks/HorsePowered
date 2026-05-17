package com.breakinblocks.horsepowered.events;

import com.breakinblocks.horsepowered.items.ModItems;
import com.breakinblocks.horsepowered.items.WorkSaddleItem;
import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class WorkSaddleEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.WORK_SADDLE.get())) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;

        Player player = event.getEntity();
        InteractionResult result = WorkSaddleItem.tryCapture(stack, player, target, event.getHand());
        if (result.consumesAction()) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
