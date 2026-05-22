package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.AnimalTrapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockAnimalTrap extends Block implements EntityBlock, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public BlockAnimalTrap(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AnimalTrapBlockEntity trap)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) return ItemInteractionResult.sidedSuccess(true);
            if (trap.releaseCapturedEntity()) {
                level.playSound(null, pos, SoundEvents.SHEEP_AMBIENT, SoundSource.BLOCKS, 0.8F, 1.0F);
                return ItemInteractionResult.sidedSuccess(false);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!stack.isEmpty()
                && trap.getInventory().getStackInSlot(AnimalTrapBlockEntity.BAIT_SLOT).isEmpty()) {
            if (level.isClientSide) return ItemInteractionResult.sidedSuccess(true);
            if (trap.tryInsertBait(stack)) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
                return ItemInteractionResult.sidedSuccess(false);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AnimalTrapBlockEntity trap)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) return InteractionResult.sidedSuccess(true);
            if (trap.releaseCapturedEntity()) {
                level.playSound(null, pos, SoundEvents.SHEEP_AMBIENT, SoundSource.BLOCKS, 0.8F, 1.0F);
                return InteractionResult.sidedSuccess(false);
            }
            return InteractionResult.PASS;
        }

        if (level.isClientSide) return InteractionResult.sidedSuccess(true);
        trap.dropInventoryContents();
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
        return InteractionResult.sidedSuccess(false);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof AnimalTrapBlockEntity trap) {
            ItemStack stack = new ItemStack(this);
            CompoundTag beTag = trap.writeBlockEntityComponent(params.getLevel().registryAccess());
            if (!beTag.isEmpty()) {
                stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(beTag));
            }
            return List.of(stack);
        }
        return super.getDrops(state, params);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnimalTrapBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlocks.ANIMAL_TRAP_BE.get()) return null;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> ticker = level.isClientSide
                ? (BlockEntityTicker<T>) (BlockEntityTicker<AnimalTrapBlockEntity>) AnimalTrapBlockEntity::clientTick
                : (BlockEntityTicker<T>) (BlockEntityTicker<AnimalTrapBlockEntity>) AnimalTrapBlockEntity::serverTick;
        return ticker;
    }
}
