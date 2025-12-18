package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockChopper extends BlockHPBase {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Bottom block shape only - filler block handles the top
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BlockChopper(Properties properties) {
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
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        // Check if there's room for the filler block above
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return null; // Can't place - no room for top block
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // Place the filler block above
        BlockPos fillerPos = pos.above();
        BlockState fillerState = ModBlocks.FILLER.get().defaultBlockState()
                .setValue(BlockFiller.FACING, Direction.DOWN); // Points down to main block
        level.setBlock(fillerPos, fillerState, 3);

        if (placer != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChopperBlockEntity chopper) {
                chopper.setForward(placer.getDirection().getOpposite());
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
        // No special action needed
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChopperBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> HPBlockEntityHorseBase.clientTick(lvl, pos, st, (ChopperBlockEntity) be);
        } else {
            return (lvl, pos, st, be) -> HPBlockEntityHorseBase.serverTick(lvl, pos, st, (ChopperBlockEntity) be);
        }
    }
}
