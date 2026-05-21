package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockHandGrindstone extends BlockHPBase {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 4, 15),
            Block.box(6.5, 4, 6.5, 9.5, 10, 9.5)
    );
    private static final VoxelShape COLLISION_SHAPE = SHAPE;

    private static final float STEM_MIN = 6.5F / 16F;
    private static final float STEM_MAX = 9.5F / 16F;

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
            if (grindstone.canWork() && !player.isShiftKeyDown() && isStemHit(pos, hit)) {
                if (!level.isClientSide) {
                    float recipeHunger = grindstone.getRecipe().map(r -> r.value().getHungerCost()).orElse(0.0F);
                    if (grindstone.turn()) {
                        player.causeFoodExhaustion(HorsePowerConfig.grindstoneExhaustion.get().floatValue() + recipeHunger);
                        playTurnFeedback(level, pos);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useWithoutItem(state, level, pos, player, hit);
    }

    private static boolean isStemHit(BlockPos pos, BlockHitResult hit) {
        double hitX = hit.getLocation().x - pos.getX();
        double hitZ = hit.getLocation().z - pos.getZ();
        return hitX >= STEM_MIN && hitX <= STEM_MAX && hitZ >= STEM_MIN && hitZ <= STEM_MAX;
    }

    private static void playTurnFeedback(Level level, BlockPos pos) {
        float pitch = 0.9F + level.getRandom().nextFloat() * 0.2F;
        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, pitch);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5,
                    4, 0.15, 0.05, 0.15, 0.0);
        }
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
        // No special action needed
    }

    @Override
    public int getSlot(BlockState state, float hitX, float hitY, float hitZ) {
        if (hitX >= STEM_MIN && hitX <= STEM_MAX && hitZ >= STEM_MIN && hitZ <= STEM_MAX) {
            return -2;
        }

        double dx = hitX - 0.5;
        double dz = hitZ - 0.5;
        Direction side;
        if (Math.abs(dx) > Math.abs(dz)) {
            side = dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            side = dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        Direction front = state.getValue(FACING);
        if (side == front.getClockWise()) return 0;
        if (side == front.getCounterClockWise()) return 1;
        if (side == front) return 2;
        return -2;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HandGrindstoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return checkType(type, ModBlocks.HAND_GRINDSTONE_BE.get(), HandGrindstoneBlockEntity::clientTick);
        }
        return checkType(type, ModBlocks.HAND_GRINDSTONE_BE.get(), HandGrindstoneBlockEntity::serverTick);
    }
}
