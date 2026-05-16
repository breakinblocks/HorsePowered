package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockChoppingBlock extends BlockHPBase {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 9, 16);

    public BlockChoppingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ManualChopperBlockEntity chopper) {
            // Axe strike: chop an already-placed log
            if (stack.is(ItemTags.AXES) && chopper.canWork()) {
                if (!level.isClientSide) {
                    float recipeHunger = chopper.getRecipe().map(r -> r.value().getHungerCost()).orElse(0.0F);
                    boolean finishedChop = chopper.chop(player, stack);
                    if (finishedChop && HorsePowerConfig.shouldDamageAxe.get()) {
                        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                        stack.hurtAndBreak(1, player, slot);
                    }
                    player.causeFoodExhaustion(HorsePowerConfig.choppingBlockExhaustion.get().floatValue() + recipeHunger);
                    playStrikeFeedback(level, pos, chopper.getItem(0));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            // Log placement: accept any item that matches a chopping recipe, insert
            // directly into slot 0 so we don't fall through to super.useItemOn (which
            // can let the item place as a block above the station).
            if (!stack.isEmpty() && chopper.getItem(0).isEmpty() && chopper.isItemValidForSlot(0, stack)) {
                if (!level.isClientSide) {
                    ItemStack placed = stack.copy();
                    placed.setCount(1);
                    chopper.setItem(0, placed);
                    stack.shrink(1);
                    playPlaceFeedback(level, pos);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    private static void playPlaceFeedback(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.9F, 0.8F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    6, 0.2, 0.05, 0.2, 0.0);
        }
    }

    private static void playStrikeFeedback(Level level, BlockPos pos, ItemStack target) {
        float pitch = 0.85F + level.getRandom().nextFloat() * 0.20F;
        level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.9F, pitch);
        if (level instanceof ServerLevel serverLevel && !target.isEmpty()) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    8, 0.25, 0.1, 0.25, 0.0);
        }
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
        // No special action needed
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManualChopperBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Manual chopper doesn't need ticking
        return null;
    }
}
