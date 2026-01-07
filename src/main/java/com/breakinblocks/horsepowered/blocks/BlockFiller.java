package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A filler block for multi-block structures (like the chopper).
 * Delegates all interactions to the main block it's paired with.
 */
public class BlockFiller extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BlockFiller(Properties properties) {
        super(properties.noOcclusion().noLootTable());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Use standard block shape - don't delegate to avoid shape offset issues
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Use standard block shape for collision
        return Shapes.block();
    }

    private boolean validateFilled(Level level, BlockPos fillerPos, BlockState fillerState) {
        BlockPos filledPos = fillerPos.relative(fillerState.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (filledState.getBlock() instanceof BlockHPBase) {
            return true;
        } else {
            level.setBlock(fillerPos, Blocks.AIR.defaultBlockState(), 3);
            return false;
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        Direction facing = state.getValue(FACING);
        if (direction == facing && !(neighborState.getBlock() instanceof BlockHPBase)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos filledPos = pos.relative(state.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (validateFilled(level, pos, state)) {
            return filledState.useItemOn(stack, level, player, hand, hit.withPosition(filledPos));
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos filledPos = pos.relative(state.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (validateFilled(level, pos, state)) {
            return filledState.useWithoutItem(level, player, hit.withPosition(filledPos));
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos filledPos = pos.relative(state.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (validateFilled(level, pos, state)) {
            level.destroyBlock(filledPos, true);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        BlockPos filledPos = pos.relative(state.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (filledState.getBlock() instanceof BlockHPBase) {
            return new ItemStack(filledState.getBlock().asItem());
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FillerBlockEntity(pos, state);
    }
}
