package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockFiller(Properties properties) {
        super(properties.noOcclusion().noLootTable());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Use standard block shape - don't delegate to avoid shape offset issues
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction == facing && !(neighborState.getBlock() instanceof BlockHPBase)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos filledPos = pos.relative(state.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (validateFilled(level, pos, state)) {
            return filledState.use(level, player, hand, hit.withPosition(filledPos));
        }
        return InteractionResult.PASS;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos filledPos = pos.relative(state.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (validateFilled(level, pos, state)) {
            level.destroyBlock(filledPos, true);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos filledPos = pos.relative(state.getValue(FACING));
        BlockState filledState = level.getBlockState(filledPos);
        if (filledState.getBlock() instanceof BlockHPBase filledBlock) {
            return filledBlock.getCloneItemStack(level, filledPos, filledState);
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FillerBlockEntity(pos, state);
    }
}
