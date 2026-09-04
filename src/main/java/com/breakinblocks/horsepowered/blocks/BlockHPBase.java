package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityBase;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public abstract class BlockHPBase extends Block implements EntityBlock {

    public BlockHPBase(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    public abstract void emptiedOutput(Level level, BlockPos pos);

    public int getSlot(BlockState state, float hitX, float hitY, float hitZ) {
        return -1;
    }

    public void onWorkerAttached(Player player, PathfinderMob creature) {
    }

    @Nullable
    protected abstract <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type);

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(level, state, type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(
            BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HPBlockEntityBase te) {
                Containers.dropContents(level, pos, te);
                level.updateNeighbourForOutputSignal(pos, this);

                // Respawn the virtual worker and drop a lead if one was attached
                if (te instanceof HPBlockEntityHorseBase horseTe) {
                    horseTe.onBlockRemoved();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof HPBlockEntityBase te)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        HPBlockEntityHorseBase horseTE = te instanceof HPBlockEntityHorseBase ? (HPBlockEntityHorseBase) te : null;

        if (horseTE != null && WorkerInteraction.tryAttachLeashed(level, pos, player, horseTE.getVirtualWorker(), this::onWorkerAttached)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Handle inserting items
        if (!stack.isEmpty() && te.isItemValidForSlot(0, stack)) {
            ItemStack inputSlot = te.getItem(0);

            if (inputSlot.isEmpty()) {
                if (!level.isClientSide) {
                    int inserted = Math.min(stack.getCount(), te.getMaxStackSize(stack));
                    te.setItem(0, stack.copyWithCount(inserted));
                    stack.shrink(inserted);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            if (HPBlockEntityBase.canCombine(inputSlot, stack)) {
                int maxTransfer = Math.min(te.getMaxStackSize(stack), stack.getMaxStackSize()) - inputSlot.getCount();
                int transferAmount = Math.min(stack.getCount(), maxTransfer);
                if (transferAmount > 0) {
                    if (!level.isClientSide) {
                        stack.shrink(transferAmount);
                        inputSlot.grow(transferAmount);
                        te.setChanged();
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            int outputSlot = findTakeSlot(te, -1, false);
            if (outputSlot < 0) {
                return ItemInteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                takeFromSlot(te, outputSlot, level, pos, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof HPBlockEntityBase te)) {
            return InteractionResult.PASS;
        }

        HPBlockEntityHorseBase horseTE = te instanceof HPBlockEntityHorseBase ? (HPBlockEntityHorseBase) te : null;

        // Show working area highlight on shift+right-click with empty hand
        if (horseTE != null && player.isShiftKeyDown()) {
            WorkerInteraction.showHighlight(level, horseTE.getVirtualWorker());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        boolean emptyHanded = player.getMainHandItem().isEmpty();

        int slot = getSlot(state, (float) hit.getLocation().x - pos.getX(),
                (float) hit.getLocation().y - pos.getY(),
                (float) hit.getLocation().z - pos.getZ());
        int takeSlot = findTakeSlot(te, slot, emptyHanded);
        boolean releaseWorker = takeSlot < 0 && horseTE != null && horseTE.hasWorker() && emptyHanded;

        if (takeSlot < 0 && !releaseWorker) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        if (takeSlot >= 0) {
            takeFromSlot(te, takeSlot, level, pos, player);
        } else {
            WorkerInteraction.tryRelease(level, player, horseTE.getVirtualWorker());
        }

        te.setChanged();
        return InteractionResult.SUCCESS;
    }

    private static int findTakeSlot(HPBlockEntityBase te, int slot, boolean emptyHanded) {
        if (slot >= 0) {
            if (slot == 0 && !emptyHanded) return -1;
            return te.getItem(slot).isEmpty() ? -1 : slot;
        }

        if (slot < -1) return -1;

        if (!te.getItem(1).isEmpty()) return 1;
        if (!te.getItem(2).isEmpty()) return 2;
        return emptyHanded && !te.getItem(0).isEmpty() ? 0 : -1;
    }

    private void takeFromSlot(HPBlockEntityBase te, int slot, Level level, BlockPos pos, Player player) {
        ItemStack result = te.removeItem(slot, te.getItem(slot).getCount());
        if (result.isEmpty()) return;

        if (slot > 0) {
            emptiedOutput(level, pos);
        }
        ItemHandlerHelper.giveItemToPlayer(player, result);
    }
}
