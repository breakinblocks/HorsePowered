package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockGrindstone extends BlockHPBase {

    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 13, 16);
    private static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public BlockGrindstone(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
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
    public void emptiedOutput(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GrindstoneBlockEntity grindstone) {
            if (grindstone.getItem(1).isEmpty() && grindstone.getItem(2).isEmpty()) {
                setFilled(level, pos, false);
            }
        }
    }

    public static void setFilled(Level level, BlockPos pos, boolean filled) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockGrindstone) {
            level.setBlock(pos, state.setValue(FILLED, filled), 3);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrindstoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> HPBlockEntityHorseBase.clientTick(lvl, pos, st, (GrindstoneBlockEntity) be);
        } else {
            return (lvl, pos, st, be) -> HPBlockEntityHorseBase.serverTick(lvl, pos, st, (GrindstoneBlockEntity) be);
        }
    }
}
