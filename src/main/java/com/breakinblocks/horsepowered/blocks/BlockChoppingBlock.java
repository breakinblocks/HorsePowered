package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ManualChopperBlockEntity chopper) {
            ItemStack held = player.getItemInHand(hand);

            // Check if player is holding an axe
            if (held.is(ItemTags.AXES) && chopper.canWork()) {
                if (!level.isClientSide) {
                    if (chopper.chop(player, held)) {
                        // Damage the axe if configured
                        if (Configs.shouldDamageAxe.get()) {
                            held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        }
                    }
                    player.causeFoodExhaustion(Configs.choppingBlockExhaustion.get().floatValue());
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.use(state, level, pos, player, hand, hit);
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
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state) {
        // Manual chopper doesn't need ticking
        return null;
    }
}
