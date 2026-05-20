package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DryingRackBlockEntity extends BlockEntity {

    public static final int SLOT_COUNT = 8;
    private static final int FINISHED = -1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] progress = new int[SLOT_COUNT];
    private final int[] recipeTime = new int[SLOT_COUNT];

    private final RackItemHandler itemHandler = new RackItemHandler();

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
                .map(holder -> holder.value().assemble(new HPRecipeInput(input), level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public boolean interactSlot(int slot, ItemStack handStack, java.util.function.Consumer<ItemStack> giveBack) {
        if (slot < 0 || slot >= SLOT_COUNT || level == null) return false;
        ItemStack present = items.get(slot);

        if (present.isEmpty()) {
            if (handStack.isEmpty()) return false;
            Optional<RecipeHolder<DryingRackRecipe>> recipe = findRecipe(handStack);
            if (recipe.isEmpty()) return false;
            ItemStack inserted = handStack.copyWithCount(1);
            items.set(slot, inserted);
            progress[slot] = 0;
            recipeTime[slot] = recipe.get().value().getTime();
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        boolean dirty = false;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (be.items.get(slot).isEmpty()) continue;
            if (be.progress[slot] == FINISHED) continue;

            int time = be.recipeTime[slot];
            if (time <= 0) {
                Optional<RecipeHolder<DryingRackRecipe>> recipe = be.findRecipe(be.items.get(slot));
                if (recipe.isEmpty()) {
                    be.progress[slot] = 0;
                    be.recipeTime[slot] = 0;
                    continue;
                }
                be.recipeTime[slot] = recipe.get().value().getTime();
                time = be.recipeTime[slot];
            }

            be.progress[slot]++;
            if (be.progress[slot] >= time) {
                Optional<RecipeHolder<DryingRackRecipe>> recipe = be.findRecipe(be.items.get(slot));
                if (recipe.isPresent()) {
                    be.items.set(slot, recipe.get().value().assemble(new HPRecipeInput(be.items.get(slot)), level.registryAccess()));
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

    private Optional<RecipeHolder<DryingRackRecipe>> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(HPRecipes.DRYING_TYPE.get(), new HPRecipeInput(stack), level);
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putIntArray("progress", progress);
        tag.putIntArray("recipeTime", recipeTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);

        int[] loadedProgress = tag.getIntArray("progress");
        int[] loadedTime = tag.getIntArray("recipeTime");
        for (int i = 0; i < SLOT_COUNT; i++) {
            progress[i] = i < loadedProgress.length ? loadedProgress[i] : 0;
            recipeTime[i] = i < loadedTime.length ? loadedTime[i] : 0;
        }
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
        public ItemStack getStackInSlot(int slot) {
            return DryingRackBlockEntity.this.items.get(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (slot < 0 || slot >= SLOT_COUNT) return stack;
            if (!DryingRackBlockEntity.this.items.get(slot).isEmpty()) return stack;
            Optional<RecipeHolder<DryingRackRecipe>> recipe = findRecipe(stack);
            if (recipe.isEmpty()) return stack;
            if (!simulate) {
                ItemStack copy = stack.copyWithCount(1);
                DryingRackBlockEntity.this.items.set(slot, copy);
                DryingRackBlockEntity.this.progress[slot] = 0;
                DryingRackBlockEntity.this.recipeTime[slot] = recipe.get().value().getTime();
                markDirtyAndSync();
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
            if (DryingRackBlockEntity.this.progress[slot] != FINISHED) return ItemStack.EMPTY;
            ItemStack current = DryingRackBlockEntity.this.items.get(slot);
            if (current.isEmpty()) return ItemStack.EMPTY;
            int take = Math.min(amount, current.getCount());
            ItemStack out = current.copyWithCount(take);
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
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (slot < 0 || slot >= SLOT_COUNT) return false;
            if (!DryingRackBlockEntity.this.items.get(slot).isEmpty()) return false;
            return findRecipe(stack).isPresent();
        }
    }
}
