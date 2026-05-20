package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class DryingRackBlockEntity extends BlockEntity implements WorldlyContainer {

    public static final int SLOT_COUNT = 8;
    private static final int FINISHED = -1;

    private static final int[] ALL_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] progress = new int[SLOT_COUNT];
    private final int[] recipeTime = new int[SLOT_COUNT];

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = items.get(i);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    items.set(i, ItemStack.EMPTY);
                }
            }
        }
        super.preRemoveSideEffects(pos, state);
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
        Optional<RecipeHolder<DryingRackRecipe>> recipe = ((RecipeManager) level.recipeAccess())
                .getRecipeFor(HPRecipes.DRYING_TYPE.get(), new HPRecipeInput(input), level);
        return recipe.map(holder -> holder.value().createResult()).orElse(ItemStack.EMPTY);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        if (!level.isClientSide()) return;
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
                    be.items.set(slot, recipe.get().value().createResult());
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
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        return ((RecipeManager) serverLevel.recipeAccess())
                .getRecipeFor(HPRecipes.DRYING_TYPE.get(), new HPRecipeInput(stack), serverLevel);
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putIntArray("progress", progress);
        output.putIntArray("recipeTime", recipeTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(input, items);

        int[] loadedProgress = input.getIntArray("progress").orElse(new int[0]);
        int[] loadedTime = input.getIntArray("recipeTime").orElse(new int[0]);
        for (int i = 0; i < SLOT_COUNT; i++) {
            progress[i] = i < loadedProgress.length ? loadedProgress[i] : 0;
            recipeTime[i] = i < loadedTime.length ? loadedTime[i] : 0;
        }
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) {
            if (items.get(slot).isEmpty()) {
                progress[slot] = 0;
                recipeTime[slot] = 0;
            }
            markDirtyAndSync();
        }
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        ItemStack out = ContainerHelper.takeItem(items, slot);
        progress[slot] = 0;
        recipeTime[slot] = 0;
        return out;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        items.set(slot, stack);
        if (stack.isEmpty()) {
            progress[slot] = 0;
            recipeTime[slot] = 0;
        } else if (recipeTime[slot] <= 0) {
            findRecipe(stack).ifPresent(r -> recipeTime[slot] = r.value().getTime());
        }
        markDirtyAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.set(i, ItemStack.EMPTY);
            progress[i] = 0;
            recipeTime[i] = 0;
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if (slot < 0 || slot >= SLOT_COUNT) return false;
        if (!items.get(slot).isEmpty()) return false;
        if (level == null || level.isClientSide()) return true;
        return findRecipe(stack).isPresent();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot >= 0 && slot < SLOT_COUNT && progress[slot] == FINISHED;
    }
}
