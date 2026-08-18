package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.HPRecipeInput;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class AnimalTrapBlockEntity extends BlockEntity {

    public static final int INVENTORY_SIZE = 6;
    public static final int BAIT_SLOT = 0;
    public static final int FIRST_DROP_SLOT = 1;
    public static final int LAST_DROP_SLOT = 5;

    public static final int DROP_INTERVAL_TICKS = 3000;
    public static final int CATCH_ROLL_INTERVAL_TICKS = 20;
    public static final float CATCH_CHANCE = 0.05F;

    private final TrapItemHandler inventory = new TrapItemHandler();

    private int trapProgress;
    private int trapTime;
    private int dropTimer;

    @Nullable
    private CompoundTag capturedEntityTag;
    @Nullable
    private EntityType<?> capturedEntityType;

    private double spin;
    private double oSpin;
    @Nullable
    private Entity displayEntity;
    @Nullable
    private CompoundTag displayEntitySourceTag;

    public AnimalTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ANIMAL_TRAP_BE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getTrapProgress() {
        return trapProgress;
    }

    public int getTrapTime() {
        return trapTime;
    }

    public int getDropTimer() {
        return dropTimer;
    }

    public boolean hasCapturedEntity() {
        return capturedEntityTag != null;
    }

    @Nullable
    public EntityType<?> getCapturedEntityType() {
        return capturedEntityType;
    }

    @Nullable
    public CompoundTag getCapturedEntityTag() {
        return capturedEntityTag == null ? null : capturedEntityTag.copy();
    }

    public double getSpin() {
        return spin;
    }

    public double getOSpin() {
        return oSpin;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AnimalTrapBlockEntity be) {
        if (level.isClientSide) return;

        if (be.capturedEntityTag != null) {
            if (be.dropTimer >= DROP_INTERVAL_TICKS) {
                ItemStack heldBait = be.inventory.getStackInSlot(BAIT_SLOT);
                if (!heldBait.isEmpty()) {
                    Optional<RecipeHolder<TrappingRecipe>> feeding = be.findFeedingRecipe(heldBait);
                    if (feeding.isPresent() && feeding.get().value().isBaitConsumed()) {
                        be.rollLootTable();
                        double chance = feeding.get().value().getBaitConsumeChance();
                        if (chance >= 100.0D || level.getRandom().nextDouble() * 100.0D < chance) {
                            ItemStack consumed = heldBait.copy();
                            consumed.shrink(1);
                            be.inventory.setStackInSlot(BAIT_SLOT, consumed);
                        }
                    } else {
                        be.rollLootTable();
                    }
                } else if (be.capturedEntityRequiresBait()) {
                    if (be.dropTimer != DROP_INTERVAL_TICKS) {
                        be.dropTimer = DROP_INTERVAL_TICKS;
                        be.markDirtyAndSync();
                    }
                    return;
                } else {
                    be.rollLootTable();
                }
                be.dropTimer = 0;
                be.markDirtyAndSync();
                return;
            }
            be.dropTimer++;
            if (be.dropTimer % 20 == 0) be.markDirtyAndSync();
            return;
        }

        ItemStack bait = be.inventory.getStackInSlot(BAIT_SLOT);
        if (bait.isEmpty()) {
            if (be.trapProgress != 0 || be.trapTime != 0) {
                be.trapProgress = 0;
                be.trapTime = 0;
                be.markDirtyAndSync();
            }
            return;
        }

        Optional<RecipeHolder<TrappingRecipe>> recipe = be.findRecipe(bait);
        if (recipe.isEmpty()) {
            if (be.trapProgress != 0 || be.trapTime != 0) {
                be.trapProgress = 0;
                be.trapTime = 0;
                be.markDirtyAndSync();
            }
            return;
        }

        if (be.trapTime == 0) {
            be.trapTime = recipe.get().value().getTime();
            be.markDirtyAndSync();
        }

        if (be.trapProgress < be.trapTime) {
            be.trapProgress++;
            if (be.trapProgress % 20 == 0) be.markDirtyAndSync();
            return;
        }

        if (level.getGameTime() % CATCH_ROLL_INTERVAL_TICKS != 0) return;
        if (!be.recipeConditionsMet(recipe.get().value(), state)) return;
        if (level.getRandom().nextFloat() < CATCH_CHANCE) {
            be.capture(recipe.get().value());
        }
    }

    public boolean recipeConditionsMet(TrappingRecipe recipe, BlockState state) {
        if (level == null) return false;
        if (recipe.getBiome().isPresent()
                && !level.getBiome(worldPosition).is(recipe.getBiome().get())) {
            return false;
        }
        if (recipe.isWaterlogged()
                && state.hasProperty(BlockStateProperties.WATERLOGGED)
                && !state.getValue(BlockStateProperties.WATERLOGGED)) {
            return false;
        }
        return true;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AnimalTrapBlockEntity be) {
        if (!level.isClientSide) return;

        be.oSpin = be.spin;

        boolean hasDisplay = be.capturedEntityTag != null
                || !be.inventory.getStackInSlot(BAIT_SLOT).isEmpty();
        if (!hasDisplay) {
            return;
        }

        double remaining = Math.max(0, be.trapTime - be.trapProgress);
        double accel;
        if (be.capturedEntityTag != null) {
            accel = 1000.0 / 200.0;
        } else {
            accel = 1000.0 / (200.0 + remaining);
        }
        be.spin = (be.spin + accel) % 360.0;
    }

    private void capture(TrappingRecipe recipe) {
        if (!(level instanceof ServerLevel sl)) return;
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(recipe.getEntityId())) return;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(recipe.getEntityId());
        Entity proto = type.create(sl);
        if (proto == null) return;

        CompoundTag tag = new CompoundTag();
        proto.save(tag);
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");
        tag.remove("UUID");
        tag.putString("id", recipe.getEntityId().toString());

        capturedEntityTag = tag;
        capturedEntityType = type;
        inventory.setStackInSlot(BAIT_SLOT, ItemStack.EMPTY);
        trapProgress = 0;
        trapTime = 0;
        dropTimer = 0;
        markDirtyAndSync();
    }

    private void rollLootTable() {
        if (!(level instanceof ServerLevel sl)) return;
        if (capturedEntityTag == null) return;

        Entity proto = EntityType.loadEntityRecursive(capturedEntityTag, sl, Function.identity());
        if (!(proto instanceof LivingEntity living)) {
            if (proto != null) proto.discard();
            return;
        }
        living.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);

        ResourceKey<LootTable> lootKey = living.getLootTable();
        LootTable table = sl.getServer().reloadableRegistries().getLootTable(lootKey);
        LootParams params = new LootParams.Builder(sl)
                .withParameter(LootContextParams.THIS_ENTITY, living)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.DAMAGE_SOURCE, sl.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        table.getRandomItems(params, this::depositDrop);
        living.discard();
    }

    private void depositDrop(ItemStack stack) {
        if (stack.isEmpty() || level == null) return;
        ItemStack remaining = stack.copy();

        // Pass 1: top up matching stacks.
        for (int slot = FIRST_DROP_SLOT; slot <= LAST_DROP_SLOT && !remaining.isEmpty(); slot++) {
            ItemStack current = inventory.getStackInSlot(slot);
            if (current.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(current, remaining)) continue;
            int limit = Math.min(inventory.getSlotLimit(slot), remaining.getMaxStackSize());
            int space = Math.max(0, limit - current.getCount());
            int take = Math.min(remaining.getCount(), space);
            if (take == 0) continue;
            ItemStack combined = current.copyWithCount(current.getCount() + take);
            inventory.setStackInSlot(slot, combined);
            remaining.shrink(take);
        }

        // Pass 2: fill empty slots.
        for (int slot = FIRST_DROP_SLOT; slot <= LAST_DROP_SLOT && !remaining.isEmpty(); slot++) {
            ItemStack current = inventory.getStackInSlot(slot);
            if (!current.isEmpty()) continue;
            int limit = Math.min(inventory.getSlotLimit(slot), remaining.getMaxStackSize());
            int take = Math.min(remaining.getCount(), limit);
            inventory.setStackInSlot(slot, remaining.copyWithCount(take));
            remaining.shrink(take);
        }

        if (!remaining.isEmpty()) {
            Block.popResource(level, worldPosition.above(), remaining);
        }
    }

    public void dropInventoryContents() {
        if (level == null) return;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        trapProgress = 0;
        trapTime = 0;
        markDirtyAndSync();
    }

    public boolean releaseCapturedEntity() {
        if (level == null || capturedEntityTag == null) return false;
        if (!(level instanceof ServerLevel sl)) return false;

        Entity entity = EntityType.loadEntityRecursive(capturedEntityTag, sl, Function.identity());
        if (entity == null) {
            capturedEntityTag = null;
            capturedEntityType = null;
            dropTimer = 0;
            markDirtyAndSync();
            return false;
        }

        float yaw = sl.getRandom().nextFloat() * 360F;
        entity.moveTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                yaw, 0F);
        sl.addFreshEntity(entity);

        capturedEntityTag = null;
        capturedEntityType = null;
        dropTimer = 0;
        markDirtyAndSync();
        return true;
    }

    public boolean tryInsertBait(ItemStack handStack) {
        if (level == null || level.isClientSide) return false;
        if (!inventory.getStackInSlot(BAIT_SLOT).isEmpty()) return false;
        if (handStack.isEmpty()) return false;
        if (capturedEntityTag != null) {
            Optional<RecipeHolder<TrappingRecipe>> feeding = findFeedingRecipe(handStack);
            if (feeding.isEmpty() || !feeding.get().value().isBaitConsumed()) return false;
        } else if (findRecipe(handStack).isEmpty()) {
            return false;
        }
        ItemStack copy = handStack.copyWithCount(1);
        inventory.setStackInSlot(BAIT_SLOT, copy);
        markDirtyAndSync();
        return true;
    }

    public Optional<RecipeHolder<TrappingRecipe>> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(HPRecipes.TRAPPING_TYPE.get(),
                new HPRecipeInput(stack), level);
    }

    private Optional<RecipeHolder<TrappingRecipe>> findFeedingRecipe(ItemStack stack) {
        ResourceLocation typeId = getCapturedEntityTypeId();
        if (typeId == null) return Optional.empty();
        Optional<RecipeHolder<TrappingRecipe>> recipe = findRecipe(stack);
        if (recipe.isPresent() && typeId.equals(recipe.get().value().getEntityId())) {
            return recipe;
        }
        return Optional.empty();
    }

    private boolean capturedEntityRequiresBait() {
        if (level == null) return false;
        ResourceLocation typeId = getCapturedEntityTypeId();
        if (typeId == null) return false;
        try {
            return level.getRecipeManager().getAllRecipesFor(HPRecipes.TRAPPING_TYPE.get()).stream()
                    .anyMatch(h -> typeId.equals(h.value().getEntityId())
                            && h.value().isBaitConsumed());
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    private ResourceLocation getCapturedEntityTypeId() {
        return capturedEntityType == null
                ? null
                : BuiltInRegistries.ENTITY_TYPE.getKey(capturedEntityType);
    }

    public CompoundTag writeBlockEntityComponent(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    public void markDirtyAndSync() {
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
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("TrapProgress", trapProgress);
        tag.putInt("TrapTime", trapTime);
        tag.putInt("DropTimer", dropTimer);
        if (capturedEntityTag != null) {
            tag.put("CapturedEntity", capturedEntityTag.copy());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        trapProgress = tag.getInt("TrapProgress");
        trapTime = tag.getInt("TrapTime");
        dropTimer = tag.getInt("DropTimer");
        if (tag.contains("CapturedEntity")) {
            capturedEntityTag = tag.getCompound("CapturedEntity").copy();
            ResourceLocation typeId = ResourceLocation.tryParse(capturedEntityTag.getString("id"));
            capturedEntityType = typeId != null && BuiltInRegistries.ENTITY_TYPE.containsKey(typeId)
                    ? BuiltInRegistries.ENTITY_TYPE.get(typeId)
                    : null;
            displayEntity = null;
            displayEntitySourceTag = null;
        } else {
            capturedEntityTag = null;
            capturedEntityType = null;
            displayEntity = null;
            displayEntitySourceTag = null;
        }
    }

    @Nullable
    public Entity getOrBuildDisplayEntity() {
        if (level == null) return null;
        if (capturedEntityTag == null) {
            displayEntity = null;
            displayEntitySourceTag = null;
            return null;
        }
        if (displayEntity == null || displayEntitySourceTag != capturedEntityTag) {
            displayEntity = EntityType.loadEntityRecursive(capturedEntityTag, level, Function.identity());
            displayEntitySourceTag = capturedEntityTag;
        }
        return displayEntity;
    }

    private class TrapItemHandler extends ItemStackHandler {

        TrapItemHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot != BAIT_SLOT) return false;
            if (level == null) return true;
            if (capturedEntityTag != null) {
                Optional<RecipeHolder<TrappingRecipe>> feeding = findFeedingRecipe(stack);
                return feeding.isPresent() && feeding.get().value().isBaitConsumed();
            }
            return findRecipe(stack).isPresent();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != BAIT_SLOT) return stack;
            if (!getStackInSlot(BAIT_SLOT).isEmpty()) return stack;
            if (capturedEntityTag != null) {
                Optional<RecipeHolder<TrappingRecipe>> feeding = findFeedingRecipe(stack);
                if (feeding.isEmpty() || !feeding.get().value().isBaitConsumed()) return stack;
            } else if (findRecipe(stack).isEmpty()) {
                return stack;
            }
            ItemStack toInsert = stack.copyWithCount(1);
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                setStackInSlot(BAIT_SLOT, toInsert);
                markDirtyAndSync();
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == BAIT_SLOT) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirtyAndSync();
        }
    }
}
