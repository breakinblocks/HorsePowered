package com.breakinblocks.horsepowered.compat.create;

import com.breakinblocks.horsepowered.blocks.WorkerInteraction;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class HorseEngineBlock extends KineticBlock implements IBE<HorseEngineBlockEntity>, ICogWheel {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(2, 6, 2, 14, 16, 14));

    public HorseEngineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        HorseEngineBlockEntity engine = getBlockEntity(level, pos);
        if (engine == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            WorkerInteraction.showHighlight(level, engine.getVirtualWorker());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (WorkerInteraction.tryAttachLeashed(level, pos, player, engine.getVirtualWorker(),
                (p, mob) -> engine.onWorkerAttached(mob))) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (stack.isEmpty() && WorkerInteraction.tryRelease(level, player, engine.getVirtualWorker())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }

    @Override
    public Class<HorseEngineBlockEntity> getBlockEntityClass() {
        return HorseEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HorseEngineBlockEntity> getBlockEntityType() {
        return CreateCompat.HORSE_ENGINE_BE.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
