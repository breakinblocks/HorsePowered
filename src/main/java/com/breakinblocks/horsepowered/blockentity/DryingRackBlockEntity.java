package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.breakinblocks.horsepowered.blocks.DryingRackPart;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class DryingRackBlockEntity extends BlockEntity {

    public static final int SLOT_COUNT = 8;
    private static final int FINISHED = -1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] progress = new int[SLOT_COUNT];
    private final int[] recipeTime = new int[SLOT_COUNT];

    private final RackItemHandler itemHandler = new RackItemHandler();
    private LazyOptional<IItemHandler> itemHandlerCap = LazyOptional.of(() -> itemHandler);

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DRYING_RACK_BE.get(), pos, state);
    }

    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY;
    }

    public int getProgress(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? progress[slot] : 0;
    }

    public int getRecipeTime(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? recipeTime[slot] : 0;
    }

    public boolean isFinished(int slot) {
        return slot >= 0 && slot < SLOT_COUNT && progress[slot] == FINISHED;
    }

    public ItemStack getOutputPreview(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT || level == null) return ItemStack.EMPTY;
        if (progress[slot] == FINISHED) return ItemStack.EMPTY;
        ItemStack input = items.get(slot);
        if (input.isEmpty()) return ItemStack.EMPTY;
        return findRecipe(input)
                .map(r -> r.assemble(new SimpleContainer(input), level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        if (!level.isClientSide) return;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (be.items.get(slot).isEmpty()) continue;
            if (be.progress[slot] == FINISHED) continue;
            int time = be.recipeTime[slot];
            if (time <= 0) continue;
            if (be.progress[slot] < time) {
                be.progress[slot]++;
            }
        }
    }

    public boolean interactSlot(int slot, ItemStack handStack, Consumer<ItemStack> giveBack) {
        if (slot < 0 || slot >= SLOT_COUNT || level == null) return false;
        ItemStack present = items.get(slot);

        if (present.isEmpty()) {
            if (handStack.isEmpty()) return false;
            Optional<DryingRackRecipe> recipe = findRecipe(handStack);
            if (recipe.isEmpty()) return false;
            ItemStack inserted = handStack.copy();
            inserted.setCount(1);
            items.set(slot, inserted);
            progress[slot] = 0;
            recipeTime[slot] = recipe.get().getTime();
            handStack.shrink(1);
            markDirtyAndSync();
            return true;
        }

        giveBack.accept(present.copy());
        items.set(slot, ItemStack.EMPTY);
        progress[slot] = 0;
        recipeTime[slot] = 0;
        markDirtyAndSync();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        boolean dirty = false;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (be.items.get(slot).isEmpty()) continue;
            if (be.progress[slot] == FINISHED) continue;

            int time = be.recipeTime[slot];
            if (time <= 0) {
                Optional<DryingRackRecipe> recipe = be.findRecipe(be.items.get(slot));
                if (recipe.isEmpty()) {
                    be.progress[slot] = 0;
                    be.recipeTime[slot] = 0;
                    continue;
                }
                be.recipeTime[slot] = recipe.get().getTime();
                time = be.recipeTime[slot];
            }

            be.progress[slot]++;
            if (be.progress[slot] >= time) {
                Optional<DryingRackRecipe> recipe = be.findRecipe(be.items.get(slot));
                if (recipe.isPresent()) {
                    be.items.set(slot, recipe.get().assemble(new SimpleContainer(be.items.get(slot)), level.registryAccess()));
                    be.progress[slot] = FINISHED;
                    be.recipeTime[slot] = 0;
                    dirty = true;
                } else {
                    be.progress[slot] = 0;
                    be.recipeTime[slot] = 0;
                }
            }
        }
        if (dirty) be.markDirtyAndSync();
    }

    private Optional<DryingRackRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(HPRecipes.DRYING_TYPE.get(), new SimpleContainer(stack), level);
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putIntArray("progress", progress);
        tag.putIntArray("recipeTime", recipeTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, items);

        int[] loadedProgress = tag.contains("progress") ? tag.getIntArray("progress") : new int[0];
        int[] loadedTime = tag.contains("recipeTime") ? tag.getIntArray("recipeTime") : new int[0];
        for (int i = 0; i < SLOT_COUNT; i++) {
            progress[i] = i < loadedProgress.length ? loadedProgress[i] : 0;
            recipeTime[i] = i < loadedTime.length ? loadedTime[i] : 0;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockState state = getBlockState();
        BlockPos pos = getBlockPos();
        if (!state.hasProperty(BlockDryingRack.FACING)) {
            return new AABB(pos);
        }
        Direction facing = state.getValue(BlockDryingRack.FACING);
        Direction rightDir = facing.getClockWise();
        int minX = Math.min(0, Math.min(facing.getStepX(), rightDir.getStepX() + facing.getStepX()));
        int maxX = Math.max(0, Math.max(facing.getStepX(), rightDir.getStepX() + facing.getStepX()));
        int minZ = Math.min(0, Math.min(facing.getStepZ(), rightDir.getStepZ() + facing.getStepZ()));
        int maxZ = Math.max(0, Math.max(facing.getStepZ(), rightDir.getStepZ() + facing.getStepZ()));
        minX = Math.min(minX, rightDir.getStepX());
        maxX = Math.max(maxX, rightDir.getStepX());
        minZ = Math.min(minZ, rightDir.getStepZ());
        maxZ = Math.max(maxZ, rightDir.getStepZ());
        return new AABB(pos.getX() + minX, pos.getY(), pos.getZ() + minZ,
                pos.getX() + maxX + 1, pos.getY() + 1, pos.getZ() + maxZ + 1);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockDryingRack.PART) && state.getValue(BlockDryingRack.PART) != DryingRackPart.MAIN && level != null) {
                BlockPos mainPos = BlockDryingRack.getMainPos(state, worldPosition);
                BlockEntity mainBe = level.getBlockEntity(mainPos);
                if (mainBe instanceof DryingRackBlockEntity main && main != this) {
                    return main.getCapability(cap, side);
                }
                return LazyOptional.empty();
            }
            return itemHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandlerCap = LazyOptional.of(() -> itemHandler);
    }

    private class RackItemHandler extends ItemStackHandler {

        RackItemHandler() {
            super(SLOT_COUNT);
        }

        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return DryingRackBlockEntity.this.items.get(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (slot < 0 || slot >= SLOT_COUNT) return stack;
            if (!DryingRackBlockEntity.this.items.get(slot).isEmpty()) return stack;
            Optional<DryingRackRecipe> recipe = findRecipe(stack);
            if (recipe.isEmpty()) return stack;
            if (!simulate) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                DryingRackBlockEntity.this.items.set(slot, copy);
                DryingRackBlockEntity.this.progress[slot] = 0;
                DryingRackBlockEntity.this.recipeTime[slot] = recipe.get().getTime();
                markDirtyAndSync();
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
            if (DryingRackBlockEntity.this.progress[slot] != FINISHED) return ItemStack.EMPTY;
            ItemStack current = DryingRackBlockEntity.this.items.get(slot);
            if (current.isEmpty()) return ItemStack.EMPTY;
            int take = Math.min(amount, current.getCount());
            ItemStack out = current.copy();
            out.setCount(take);
            if (!simulate) {
                current.shrink(take);
                if (current.isEmpty()) {
                    DryingRackBlockEntity.this.items.set(slot, ItemStack.EMPTY);
                    DryingRackBlockEntity.this.progress[slot] = 0;
                    DryingRackBlockEntity.this.recipeTime[slot] = 0;
                }
                markDirtyAndSync();
            }
            return out;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (slot < 0 || slot >= SLOT_COUNT) return false;
            if (!DryingRackBlockEntity.this.items.get(slot).isEmpty()) return false;
            return findRecipe(stack).isPresent();
        }
    }
}
