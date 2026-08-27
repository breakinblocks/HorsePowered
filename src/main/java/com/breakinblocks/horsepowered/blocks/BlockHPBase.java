package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityBase;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.breakinblocks.horsepowered.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HPBlockEntityBase te) {
                Containers.dropContents(level, pos, te);
                level.updateNeighbourForOutputSignal(pos, this);

                if (te instanceof HPBlockEntityHorseBase horseTe && horseTe.hasWorker()) {
                    horseTe.releaseWorkerToWorld();
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(Items.LEAD));
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof HPBlockEntityBase te)) {
            return InteractionResult.PASS;
        }

        HPBlockEntityHorseBase horseTE = te instanceof HPBlockEntityHorseBase ? (HPBlockEntityHorseBase) te : null;

        // Show working area highlight on shift+right-click with empty hand
        if (horseTE != null && player.isShiftKeyDown() && stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (level.isClientSide) {
                horseTE.showWorkingAreaHighlight();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Check for leashed creatures nearby (for horse-powered blocks)
        if (horseTE != null && !horseTE.hasWorker()) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            List<PathfinderMob> creatures = Utils.getValidCreatures(level,
                    new AABB(x - 7.0D, y - 7.0D, z - 7.0D, x + 7.0D, y + 7.0D, z + 7.0D));

            for (PathfinderMob mob : creatures) {
                if (mob.isLeashed() && mob.getLeashHolder() == player) {
                    if (!level.isClientSide) {
                        mob.dropLeash(true, false);
                        horseTE.setWorker(mob);
                        onWorkerAttached(player, mob);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        // Handle inserting items
        boolean inputFull = false;
        if (!stack.isEmpty() && te.isItemValidForSlot(0, stack)) {
            ItemStack inputSlot = te.getItem(0);

            if (inputSlot.isEmpty()) {
                if (!level.isClientSide) {
                    int inserted = Math.min(stack.getCount(), te.getMaxStackSize(stack));
                    ItemStack placed = stack.copy();
                    placed.setCount(inserted);
                    te.setItem(0, placed);
                    stack.shrink(inserted);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
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
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            inputFull = true;
        }

        // Handle extracting items
        int slot = getSlot(state, (float) hit.getLocation().x - pos.getX(),
                (float) hit.getLocation().y - pos.getY(),
                (float) hit.getLocation().z - pos.getZ());

        // The client mirrors the outcome without touching anything; the server is authoritative.
        if (level.isClientSide) {
            boolean extracts;
            if (slot > -1) {
                extracts = !te.getItem(slot).isEmpty();
            } else if (slot > -2) {
                extracts = !te.getItem(1).isEmpty() || !te.getItem(2).isEmpty()
                        || (stack.isEmpty() && hand != InteractionHand.OFF_HAND && !te.getItem(0).isEmpty());
            } else {
                extracts = false;
            }
            if (!extracts) {
                if (inputFull) {
                    return InteractionResult.CONSUME;
                }
                if (!stack.isEmpty()) {
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.sidedSuccess(true);
        }

        ItemStack result = ItemStack.EMPTY;
        if (slot > -1) {
            result = te.removeItem(slot, te.getItem(slot).getCount());
        } else if (slot > -2) {
            // Try output slot first, then secondary, then input
            result = te.removeItem(1, te.getItem(1).getCount());
            if (result.isEmpty()) {
                result = te.removeItem(2, te.getItem(2).getCount());
                if (result.isEmpty() && stack.isEmpty() && hand != InteractionHand.OFF_HAND) {
                    result = te.removeItem(0, te.getItem(0).getCount());
                }
            }
            if (!result.isEmpty()) {
                emptiedOutput(level, pos);
            }
        }

        if (result.isEmpty()) {
            if (inputFull) {
                return InteractionResult.CONSUME;
            }
            if (!stack.isEmpty()) {
                return InteractionResult.PASS;
            }
            // Release worker if no other action
            if (horseTE != null) {
                horseTE.setWorkerToPlayer(player);
            }
        } else {
            ItemHandlerHelper.giveItemToPlayer(player, result);
        }

        te.setChanged();
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
