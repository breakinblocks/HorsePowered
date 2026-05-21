package com.breakinblocks.horsepowered.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FlintAndTinderItem extends Item {

    public static final int USE_DURATION_TICKS = 40;
    private static final double REACH = 4.5;

    public FlintAndTinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        player.startUsingItem(context.getHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(entity instanceof Player player)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockHitResult hit = raytrace(level, player);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        Vec3 loc = hit.getLocation();
        serverLevel.sendParticles(ParticleTypes.SMOKE, loc.x, loc.y, loc.z, 1, 0.02, 0.02, 0.02, 0.005);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;
        BlockHitResult hit = raytrace(level, player);
        if (hit.getType() != HitResult.Type.BLOCK) return stack;

        InteractionHand hand = player.getUsedItemHand();
        BlockPos hitPos = hit.getBlockPos();
        BlockState state = level.getBlockState(hitPos);

        if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
            level.playSound(player, hitPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(hitPos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, hitPos);
            stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(hand));
        } else {
            BlockPos firePos = hitPos.relative(hit.getDirection());
            if (BaseFireBlock.canBePlacedAt(level, firePos, player.getDirection())) {
                level.playSound(player, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState fireState = BaseFireBlock.getState(level, firePos);
                level.setBlock(firePos, fireState, 11);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, firePos);
                if (player instanceof ServerPlayer sp) {
                    CriteriaTriggers.PLACED_BLOCK.trigger(sp, firePos, stack);
                }
                stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(hand));
            }
        }
        return stack;
    }

    private static BlockHitResult raytrace(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(REACH));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}
