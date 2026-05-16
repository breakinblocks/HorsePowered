package com.breakinblocks.horsepowered.items;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public class WorkSaddleItem extends Item {

    public static final TagKey<EntityType<?>> VALID_WORKER =
            TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, HorsePowerMod.id("valid_worker"));

    public WorkSaddleItem(Properties properties) {
        super(properties);
    }

    public static InteractionResult tryCapture(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = target.level();
        CustomData stored = stack.get(DataComponents.ENTITY_DATA);
        if (stored != null && !stored.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(target.getType()).is(VALID_WORKER)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        target.ejectPassengers();
        if (target instanceof Leashable leashable && leashable.isLeashed()) {
            leashable.dropLeash(true, false);
        }

        CompoundTag tag = new CompoundTag();
        if (!target.save(tag)) {
            return InteractionResult.FAIL;
        }

        ItemStack result = stack.copyWithCount(1);
        CustomData.set(DataComponents.ENTITY_DATA, result, tag);

        if (stack.getCount() <= 1) {
            player.setItemInHand(hand, result);
        } else {
            stack.shrink(1);
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
        }

        target.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        CustomData stored = stack.get(DataComponents.ENTITY_DATA);
        if (stored == null || stored.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos spawnPos = clickedState.getCollisionShape(level, clickedPos).isEmpty()
                ? clickedPos
                : clickedPos.relative(face);

        CompoundTag tag = stored.copyTag();
        Optional<Entity> created = EntityType.create(tag, serverLevel);
        if (created.isEmpty()) {
            return InteractionResult.FAIL;
        }
        Entity entity = created.get();

        Player player = context.getPlayer();
        if (serverLevel.getEntity(entity.getUUID()) != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("item.horsepowered.work_saddle.duplicate")
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            return InteractionResult.FAIL;
        }

        float yRot = player != null ? player.getYRot() : 0f;
        entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, yRot, 0f);
        if (entity instanceof PathfinderMob mob) {
            mob.setYBodyRot(yRot);
        }

        if (!serverLevel.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        ItemStack emptied = stack.copyWithCount(1);
        emptied.remove(DataComponents.ENTITY_DATA);
        if (player == null || stack.getCount() <= 1) {
            stack.remove(DataComponents.ENTITY_DATA);
        } else {
            stack.shrink(1);
            if (!player.getInventory().add(emptied)) {
                player.drop(emptied, false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CustomData stored = stack.get(DataComponents.ENTITY_DATA);
        if (stored == null || stored.isEmpty()) return;

        String idStr = stored.copyTag().getString("id");
        if (idStr.isEmpty()) return;

        ResourceLocation id = ResourceLocation.tryParse(idStr);
        if (id == null) return;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (type == null) return;

        tooltip.add(Component.translatable("item.horsepowered.work_saddle.contains",
                Component.translatable(type.getDescriptionId()))
                .withStyle(ChatFormatting.GRAY));
    }
}
