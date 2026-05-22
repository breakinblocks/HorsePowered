package com.breakinblocks.horsepowered.blockentity;

import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class AnimalTrapBlockEntity extends HPBlockEntityBase {

    public static final int INVENTORY_SIZE = 6;
    public static final int BAIT_SLOT = 0;
    public static final int FIRST_DROP_SLOT = 1;
    public static final int LAST_DROP_SLOT = 5;
    private static final int[] DROP_SLOTS = { 1, 2, 3, 4, 5 };
    private static final int[] BAIT_ONLY = { 0 };

    public static final int DROP_INTERVAL_TICKS = 3000;
    public static final int CATCH_ROLL_INTERVAL_TICKS = 20;
    public static final float CATCH_CHANCE = 0.05F;

    private int trapProgress;
    private int trapTime;
    private int dropTimer;

    @Nullable
    private CompoundTag capturedEntityTag;
    @Nullable
    private Identifier capturedEntityTypeId;
    @Nullable
    private EntityType<?> capturedEntityType;

    private double spin;
    private double oSpin;
    @Nullable
    private Entity displayEntity;
    @Nullable
    private CompoundTag displayEntitySourceTag;

    public AnimalTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANIMAL_TRAP.get(), pos, state, INVENTORY_SIZE);
    }

    public int getTrapProgress() { return trapProgress; }
    public int getTrapTime() { return trapTime; }
    public int getDropTimer() { return dropTimer; }
    public boolean hasCapturedEntity() { return capturedEntityTag != null; }
    @Nullable public EntityType<?> getCapturedEntityType() { return capturedEntityType; }
    public double getSpin() { return spin; }
    public double getOSpin() { return oSpin; }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public int getOutputSlot() { return 1; }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != BAIT_SLOT) return false;
        if (!getItem(BAIT_SLOT).isEmpty()) return false;
        if (level != null && level.isClientSide()) return true;
        if (capturedEntityTag != null) {
            if (!HorsePowerConfig.animalTrapBaitConsumed.get()) return false;
            return findFeedingRecipe(stack).isPresent();
        }
        return findRecipe(HPRecipes.TRAPPING_TYPE.get(), stack).isPresent();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? DROP_SLOTS : BAIT_ONLY;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return index == BAIT_SLOT && isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= FIRST_DROP_SLOT && index <= LAST_DROP_SLOT;
    }

    public Optional<RecipeHolder<TrappingRecipe>> findTrappingRecipe(ItemStack stack) {
        return findRecipe(HPRecipes.TRAPPING_TYPE.get(), stack);
    }

    private Optional<RecipeHolder<TrappingRecipe>> findFeedingRecipe(ItemStack stack) {
        if (capturedEntityTypeId == null) return Optional.empty();
        Optional<RecipeHolder<TrappingRecipe>> recipe = findRecipe(HPRecipes.TRAPPING_TYPE.get(), stack);
        if (recipe.isPresent() && capturedEntityTypeId.equals(recipe.get().value().getEntityId())) {
            return recipe;
        }
        return Optional.empty();
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AnimalTrapBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.capturedEntityTag != null) {
            if (be.dropTimer >= DROP_INTERVAL_TICKS) {
                if (HorsePowerConfig.animalTrapBaitConsumed.get()) {
                    ItemStack bait = be.getItem(BAIT_SLOT);
                    if (bait.isEmpty()) {
                        if (be.dropTimer != DROP_INTERVAL_TICKS) {
                            be.dropTimer = DROP_INTERVAL_TICKS;
                            be.setChanged();
                        }
                        return;
                    }
                    be.rollLootTable();
                    double chance = HorsePowerConfig.animalTrapBaitConsumeChance.get();
                    if (chance >= 100.0D || level.getRandom().nextDouble() * 100.0D < chance) {
                        bait.shrink(1);
                        if (bait.isEmpty()) be.setItem(BAIT_SLOT, ItemStack.EMPTY);
                    }
                } else {
                    be.rollLootTable();
                }
                be.dropTimer = 0;
                be.setChanged();
                return;
            }
            be.dropTimer++;
            if (be.dropTimer % 20 == 0) {
                be.setChanged();
            }
            return;
        }

        ItemStack bait = be.getItem(BAIT_SLOT);
        if (bait.isEmpty()) {
            if (be.trapProgress != 0 || be.trapTime != 0) {
                be.trapProgress = 0;
                be.trapTime = 0;
                be.setChanged();
            }
            return;
        }

        Optional<RecipeHolder<TrappingRecipe>> recipe = be.findTrappingRecipe(bait);
        if (recipe.isEmpty()) {
            if (be.trapProgress != 0 || be.trapTime != 0) {
                be.trapProgress = 0;
                be.trapTime = 0;
                be.setChanged();
            }
            return;
        }

        if (be.trapTime == 0) {
            be.trapTime = recipe.get().value().getTime();
            be.setChanged();
        }

        if (be.trapProgress < be.trapTime) {
            be.trapProgress++;
            if (be.trapProgress % 20 == 0) be.setChanged();
            return;
        }

        if (level.getGameTime() % CATCH_ROLL_INTERVAL_TICKS != 0) return;
        if (!be.recipeConditionsMet(recipe.get().value(), state)) return;
        if (level.getRandom().nextFloat() < CATCH_CHANCE) {
            be.capture(recipe.get().value());
        }
    }

    public static void clientTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AnimalTrapBlockEntity be) {
        if (!level.isClientSide()) return;
        be.oSpin = be.spin;
        boolean hasDisplay = be.capturedEntityTag != null || !be.getItem(BAIT_SLOT).isEmpty();
        if (!hasDisplay) return;
        double remaining = Math.max(0, be.trapTime - be.trapProgress);
        double accel = be.capturedEntityTag != null
                ? 1000.0 / 200.0
                : 1000.0 / (200.0 + remaining);
        be.spin = (be.spin + accel) % 360.0;
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

    private void capture(TrappingRecipe recipe) {
        if (!(level instanceof ServerLevel sl)) return;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(recipe.getEntityId());
        if (type == null) return;
        Entity proto = type.create(sl, EntitySpawnReason.LOAD);
        if (proto == null) return;

        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, sl.registryAccess());
        proto.saveAsPassenger(output);
        CompoundTag tag = output.buildResult();
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");
        tag.remove("UUID");
        tag.putString("id", recipe.getEntityId().toString());

        capturedEntityTag = tag;
        capturedEntityType = type;
        capturedEntityTypeId = recipe.getEntityId();
        proto.discard();
        setItem(BAIT_SLOT, ItemStack.EMPTY);
        trapProgress = 0;
        trapTime = 0;
        dropTimer = 0;
        setChanged();
    }

    private void rollLootTable() {
        if (!(level instanceof ServerLevel sl)) return;
        if (capturedEntityTag == null || capturedEntityType == null) return;

        Entity proto = capturedEntityType.create(sl, EntitySpawnReason.LOAD);
        if (!(proto instanceof LivingEntity living)) {
            if (proto != null) proto.discard();
            return;
        }
        try {
            ValueInput input = TagValueInput.create(
                    ProblemReporter.DISCARDING, sl.registryAccess(), capturedEntityTag);
            living.load(input);
        } catch (Exception ignored) {
            // Fall through with default-loaded entity.
        }
        living.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);

        Optional<ResourceKey<LootTable>> lootKeyOpt = living.getLootTable();
        if (lootKeyOpt.isEmpty()) {
            living.discard();
            return;
        }
        LootTable table = sl.getServer().reloadableRegistries().getLootTable(lootKeyOpt.get());
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

        for (int slot = FIRST_DROP_SLOT; slot <= LAST_DROP_SLOT && !remaining.isEmpty(); slot++) {
            ItemStack current = getItem(slot);
            if (current.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(current, remaining)) continue;
            int limit = Math.min(getInventoryStackLimit(), remaining.getMaxStackSize());
            int space = Math.max(0, limit - current.getCount());
            int take = Math.min(remaining.getCount(), space);
            if (take == 0) continue;
            current.grow(take);
            remaining.shrink(take);
        }

        for (int slot = FIRST_DROP_SLOT; slot <= LAST_DROP_SLOT && !remaining.isEmpty(); slot++) {
            ItemStack current = getItem(slot);
            if (!current.isEmpty()) continue;
            int limit = Math.min(getInventoryStackLimit(), remaining.getMaxStackSize());
            int take = Math.min(remaining.getCount(), limit);
            itemStacks.set(slot, remaining.copyWithCount(take));
            remaining.shrink(take);
        }

        if (!remaining.isEmpty()) {
            Block.popResource(level, worldPosition.above(), remaining);
        }
        setChanged();
    }

    public void dropInventoryContents() {
        if (level == null) return;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        stack.copy());
                itemStacks.set(slot, ItemStack.EMPTY);
            }
        }
        trapProgress = 0;
        trapTime = 0;
        setChanged();
    }

    public boolean releaseCapturedEntity() {
        if (level == null || capturedEntityTag == null) return false;
        if (!(level instanceof ServerLevel sl)) return false;

        Entity entity = null;
        if (capturedEntityType != null) {
            entity = capturedEntityType.create(sl, EntitySpawnReason.LOAD);
            if (entity != null) {
                try {
                    ValueInput input = TagValueInput.create(
                            ProblemReporter.DISCARDING, sl.registryAccess(), capturedEntityTag);
                    entity.load(input);
                } catch (Exception ignored) {
                    // proceed with default-loaded entity
                }
            }
        }

        if (entity == null) {
            capturedEntityTag = null;
            capturedEntityType = null;
            capturedEntityTypeId = null;
            dropTimer = 0;
            setChanged();
            return false;
        }

        float yaw = sl.getRandom().nextFloat() * 360F;
        entity.snapTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                yaw, 0F);
        sl.addFreshEntity(entity);

        capturedEntityTag = null;
        capturedEntityType = null;
        capturedEntityTypeId = null;
        dropTimer = 0;
        setChanged();
        return true;
    }

    public boolean tryInsertBait(ItemStack handStack) {
        if (level == null || level.isClientSide()) return false;
        if (!getItem(BAIT_SLOT).isEmpty()) return false;
        if (handStack.isEmpty()) return false;
        if (capturedEntityTag != null) {
            if (!HorsePowerConfig.animalTrapBaitConsumed.get()) return false;
            if (findFeedingRecipe(handStack).isEmpty()) return false;
        } else if (findTrappingRecipe(handStack).isEmpty()) {
            return false;
        }
        ItemStack copy = handStack.copyWithCount(1);
        setItem(BAIT_SLOT, copy);
        return true;
    }

    @Nullable
    public CompoundTag getCapturedEntityTag() {
        return capturedEntityTag == null ? null : capturedEntityTag.copy();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("TrapProgress", trapProgress);
        output.putInt("TrapTime", trapTime);
        output.putInt("DropTimer", dropTimer);
        if (capturedEntityTag != null) {
            output.store("CapturedEntity", CompoundTag.CODEC, capturedEntityTag);
        }
        if (capturedEntityTypeId != null) {
            output.putString("CapturedEntityId", capturedEntityTypeId.toString());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        trapProgress = input.getIntOr("TrapProgress", 0);
        trapTime = input.getIntOr("TrapTime", 0);
        dropTimer = input.getIntOr("DropTimer", 0);

        Optional<CompoundTag> tag = input.read("CapturedEntity", CompoundTag.CODEC);
        if (tag.isPresent()) {
            capturedEntityTag = tag.get();
            String idStr = input.getStringOr("CapturedEntityId", "");
            if (!idStr.isEmpty()) {
                Identifier id = Identifier.tryParse(idStr);
                capturedEntityTypeId = id;
                capturedEntityType = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(id);
            }
        } else {
            capturedEntityTag = null;
            capturedEntityType = null;
            capturedEntityTypeId = null;
        }
        displayEntity = null;
        displayEntitySourceTag = null;
    }

    @Nullable
    public Entity getOrBuildDisplayEntity() {
        if (level == null || capturedEntityTag == null || capturedEntityType == null) {
            displayEntity = null;
            displayEntitySourceTag = null;
            return null;
        }
        if (displayEntity == null || displayEntitySourceTag != capturedEntityTag) {
            try {
                Entity built = capturedEntityType.create(level, EntitySpawnReason.LOAD);
                if (built != null) {
                    ValueInput input = TagValueInput.create(
                            ProblemReporter.DISCARDING, level.registryAccess(), capturedEntityTag);
                    built.load(input);
                    displayEntity = built;
                    displayEntitySourceTag = capturedEntityTag;
                }
            } catch (Exception e) {
                displayEntity = null;
            }
        }
        return displayEntity;
    }
}
