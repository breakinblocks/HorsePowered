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
import net.minecraft.world.item.LeadItem;
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
    protected abstract <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state);

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(level, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HPBlockEntityBase te) {
                Containers.dropContents(level, pos, te);
                level.updateNeighbourForOutputSignal(pos, this);

                // Drop lead if horse was attached
                if (te instanceof HPBlockEntityHorseBase horseTe && horseTe.hasWorker()) {
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
        PathfinderMob creature = null;
        if (horseTE != null) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            List<PathfinderMob> creatures = Utils.getValidCreatures(level,
                    new AABB(x - 7.0D, y - 7.0D, z - 7.0D, x + 7.0D, y + 7.0D, z + 7.0D));

            for (PathfinderMob mob : creatures) {
                if (mob.isLeashed() && mob.getLeashHolder() == player) {
                    creature = mob;
                    break;
                }
            }
        }

        // Handle attaching a leashed creature
        if (horseTE != null && ((stack.getItem() instanceof LeadItem && creature != null) || creature != null)) {
            if (!horseTE.hasWorker()) {
                // Detach leash but don't drop it - give the lead back to the player
                creature.dropLeash(true, false);
                if (!level.isClientSide) {
                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.LEAD));
                }
                horseTE.setWorker(creature);
                onWorkerAttached(player, creature);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.FAIL;
        }

        // Handle inserting items
        if (!stack.isEmpty() && te.isItemValidForSlot(0, stack)) {
            ItemStack inputSlot = te.getItem(0);

            if (inputSlot.isEmpty()) {
                te.setItem(0, stack.copy());
                stack.setCount(stack.getCount() - te.getMaxStackSize(stack));
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (HPBlockEntityBase.canCombine(inputSlot, stack)) {
                int maxTransfer = Math.min(te.getMaxStackSize(stack), stack.getMaxStackSize()) - inputSlot.getCount();
                int transferAmount = Math.min(stack.getCount(), maxTransfer);
                if (transferAmount > 0) {
                    stack.shrink(transferAmount);
                    inputSlot.grow(transferAmount);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        // Handle extracting items
        int slot = getSlot(state, (float) hit.getLocation().x - pos.getX(),
                (float) hit.getLocation().y - pos.getY(),
                (float) hit.getLocation().z - pos.getZ());

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
            if (!stack.isEmpty()) {
                return InteractionResult.PASS;
            }
            // Release worker if no other action
            if (horseTE != null) {
                horseTE.setWorkerToPlayer(player);
            }
        }

        if (!result.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, result);
        }

        te.setChanged();
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
