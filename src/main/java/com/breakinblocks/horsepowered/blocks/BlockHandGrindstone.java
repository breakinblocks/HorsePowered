package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockHandGrindstone extends BlockHPBase {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);
    private static final VoxelShape COLLISION_SHAPE = Block.box(1, 0, 1, 15, 10, 15);

    public BlockHandGrindstone(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HandGrindstoneBlockEntity grindstone) {
                grindstone.setForward(placer.getDirection().getOpposite());
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof HandGrindstoneBlockEntity grindstone) {
            // If grindstone can work and player isn't sneaking, turn it
            if (grindstone.canWork() && !player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    if (grindstone.turn()) {
                        player.causeFoodExhaustion(HorsePowerConfig.grindstoneExhaustion.get().floatValue());
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
        // No special action needed
    }

    @Override
    public int getSlot(BlockState state, float hitX, float hitY, float hitZ) {
        Direction facing = state.getValue(FACING).getOpposite();

        // Determine which slot based on hit position and facing
        if (hitX >= 0.3125 && hitX <= 0.6875 && hitY >= 0.52 && hitZ >= 0.625 && hitZ <= 0.9375) {
            return facing == Direction.NORTH ? 2 : facing == Direction.SOUTH ? -2 : facing == Direction.EAST ? 1 : 0;
        } else if (hitX >= 0.3125 && hitX <= 0.6875 && hitY >= 0.52 && hitZ >= 0.0625 && hitZ <= 0.375) {
            return facing == Direction.NORTH ? -2 : facing == Direction.SOUTH ? 2 : facing == Direction.EAST ? 0 : 1;
        } else if (hitX >= 0.0625 && hitX <= 0.375 && hitY >= 0.52 && hitZ >= 0.3125 && hitZ <= 0.6875) {
            return facing == Direction.NORTH ? 0 : facing == Direction.SOUTH ? 1 : facing == Direction.EAST ? 2 : -2;
        } else if (hitX >= 0.625 && hitX <= 0.9375 && hitY >= 0.52 && hitZ >= 0.3125 && hitZ <= 0.6875) {
            return facing == Direction.NORTH ? 1 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? -2 : 2;
        }

        return -2;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HandGrindstoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> HandGrindstoneBlockEntity.clientTick(lvl, pos, st, (HandGrindstoneBlockEntity) be);
        } else {
            return (lvl, pos, st, be) -> HandGrindstoneBlockEntity.serverTick(lvl, pos, st, (HandGrindstoneBlockEntity) be);
        }
    }
}
