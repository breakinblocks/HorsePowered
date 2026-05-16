package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.GeneratorBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockGenerator extends BlockHPBase {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape SHAPE = Shapes.block();
    private static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public BlockGenerator(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
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
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return checkType(type, ModBlocks.GENERATOR_BE.get(), HPBlockEntityHorseBase::clientTick);
        }
        return checkType(type, ModBlocks.GENERATOR_BE.get(), HPBlockEntityHorseBase::serverTick);
    }
}
