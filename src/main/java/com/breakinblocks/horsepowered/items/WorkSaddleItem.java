package com.breakinblocks.horsepowered.items;

import com.breakinblocks.horsepowered.HorsePowerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.function.Consumer;

public class WorkSaddleItem extends Item {

    public static final TagKey<EntityType<?>> VALID_WORKER =
            TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, HorsePowerMod.id("valid_worker"));

    public WorkSaddleItem(Properties properties) {
        super(properties);
    }

    public static InteractionResult tryCapture(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = target.level();
        if (stack.has(DataComponents.ENTITY_DATA)) {
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
            leashable.dropLeash();
        }

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        target.saveWithoutId(output);
        TypedEntityData<EntityType<?>> data = TypedEntityData.of(target.getType(), output.buildResult());

        ItemStack result = stack.copyWithCount(1);
        result.set(DataComponents.ENTITY_DATA, data);

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
        TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
        if (data == null) {
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

        EntityType<?> type = data.type();
        Entity entity = type.create(serverLevel, EntitySpawnReason.LOAD);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        try (ProblemReporter.ScopedCollector reporter =
                     new ProblemReporter.ScopedCollector(entity.problemPath(), HorsePowerMod.LOGGER)) {
            TagValueOutput baseOut = TagValueOutput.createWithContext(reporter, serverLevel.registryAccess());
            entity.saveWithoutId(baseOut);
            var merged = baseOut.buildResult();
            merged.merge(data.copyTagWithoutId());
            entity.load(TagValueInput.create(reporter, serverLevel.registryAccess(), merged));
        }

        Player player = context.getPlayer();

        if (serverLevel.getEntity(entity.getUUID()) != null) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("item.horsepowered.work_saddle.duplicate")
                                .withStyle(net.minecraft.ChatFormatting.RED),
                        true);
            }
            return InteractionResult.FAIL;
        }

        float yRot = player != null ? player.getYRot() : 0f;
        entity.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, yRot, 0f);

        if (!serverLevel.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        ItemStack result = stack.copyWithCount(1);
        result.remove(DataComponents.ENTITY_DATA);
        if (player == null || stack.getCount() <= 1) {
            stack.remove(DataComponents.ENTITY_DATA);
        } else {
            stack.shrink(1);
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, consumer, flag);
        TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
        if (data != null) {
            consumer.accept(Component.translatable("item.horsepowered.work_saddle.contains",
                    Component.translatable(data.type().getDescriptionId()))
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
}
