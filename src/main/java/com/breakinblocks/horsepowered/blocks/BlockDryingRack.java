package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockDryingRack extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DryingRackPart> PART = EnumProperty.create("part", DryingRackPart.class);

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public BlockDryingRack(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, DryingRackPart.MAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Level level = context.getLevel();
        BlockPos mainPos = context.getClickedPos();
        for (DryingRackPart fillerPart : DryingRackPart.FILLERS) {
            BlockPos fillerPos = fillerPart.offsetFromMain(mainPos, facing);
            if (!level.getBlockState(fillerPos).canBeReplaced(context)) return null;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(PART, DryingRackPart.MAIN);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        for (DryingRackPart fillerPart : DryingRackPart.FILLERS) {
            BlockPos fillerPos = fillerPart.offsetFromMain(pos, facing);
            level.setBlock(fillerPos, defaultBlockState().setValue(FACING, facing).setValue(PART, fillerPart), 3);
        }
    }

    public static BlockPos getMainPos(BlockState state, BlockPos thisPos) {
        return state.getValue(PART).mainFrom(thisPos, state.getValue(FACING));
    }

    @Nullable
    public static DryingRackBlockEntity getMainBlockEntity(Level level, BlockState state, BlockPos thisPos) {
        BlockPos mainPos = getMainPos(state, thisPos);
        BlockEntity be = level.getBlockEntity(mainPos);
        return be instanceof DryingRackBlockEntity rack ? rack : null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.TRY_WITH_EMPTY_HAND;
        DryingRackBlockEntity rack = getMainBlockEntity(level, state, pos);
        if (rack == null) return InteractionResult.TRY_WITH_EMPTY_HAND;

        int slot = slotForHit(state, pos, hit);
        if (slot < 0) return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (!level.isClientSide()) {
            rack.interactSlot(slot, stack, give -> {
                if (!player.getInventory().add(give)) {
                    player.drop(give, false);
                }
            });
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        DryingRackBlockEntity rack = getMainBlockEntity(level, state, pos);
        if (rack == null) return InteractionResult.PASS;

        int slot = slotForHit(state, pos, hit);
        if (slot < 0) return InteractionResult.PASS;
        if (rack.getItem(slot).isEmpty()) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            rack.interactSlot(slot, ItemStack.EMPTY, give -> {
                if (!player.getInventory().add(give)) {
                    player.drop(give, false);
                }
            });
        }
        return InteractionResult.SUCCESS;
    }

    public static int slotForHit(BlockState state, BlockPos pos, BlockHitResult hit) {
        Vec3 loc = hit.getLocation();
        BlockPos mainPos = getMainPos(state, pos);
        Direction facing = state.getValue(FACING);

        double mainX = loc.x - mainPos.getX();
        double mainY = loc.y - mainPos.getY();
        double mainZ = loc.z - mainPos.getZ();
        if (mainY < 11.0 / 16.0) return -1;

        Direction rightDir = facing.getClockWise();
        Direction forwardDir = facing;

        double rightCoord = (mainX - 0.5) * rightDir.getStepX() + (mainZ - 0.5) * rightDir.getStepZ();
        double forwardCoord = (mainX - 0.5) * forwardDir.getStepX() + (mainZ - 0.5) * forwardDir.getStepZ();

        double rightT = rightCoord + 0.5;
        double forwardT = forwardCoord + 0.5;
        if (rightT < 0 || rightT > 2 || forwardT < 0 || forwardT > 2) return -1;

        int row = Math.min(3, Math.max(0, (int) (forwardT * 2.0)));
        int col = rightT < 1.0 ? 0 : 1;
        return row * 2 + col;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(PART) == DryingRackPart.MAIN) {
            for (DryingRackPart fillerPart : DryingRackPart.FILLERS) {
                BlockPos fillerPos = fillerPart.offsetFromMain(pos, facing);
                BlockState fillerState = level.getBlockState(fillerPos);
                if (fillerState.is(this) && fillerState.getValue(PART) == fillerPart) {
                    level.destroyBlock(fillerPos, false);
                }
            }
        } else {
            BlockPos mainPos = getMainPos(state, pos);
            BlockState mainState = level.getBlockState(mainPos);
            if (mainState.is(this) && mainState.getValue(PART) == DryingRackPart.MAIN) {
                level.destroyBlock(mainPos, true);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == DryingRackPart.MAIN ? new DryingRackBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(PART) != DryingRackPart.MAIN) return null;
        if (type != ModBlockEntities.DRYING_RACK.get()) return null;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> ticker = level.isClientSide()
                ? (BlockEntityTicker<T>) (BlockEntityTicker<DryingRackBlockEntity>) DryingRackBlockEntity::clientTick
                : (BlockEntityTicker<T>) (BlockEntityTicker<DryingRackBlockEntity>) DryingRackBlockEntity::serverTick;
        return ticker;
    }
}
