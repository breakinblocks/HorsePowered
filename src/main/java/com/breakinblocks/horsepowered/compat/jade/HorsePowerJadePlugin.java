package com.breakinblocks.horsepowered.compat.jade;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import com.breakinblocks.horsepowered.blockentity.AnimalTrapBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GraniteAnvilBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityBase;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.blocks.BlockChoppingBlock;
import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.breakinblocks.horsepowered.blocks.BlockFiller;
import com.breakinblocks.horsepowered.blocks.BlockAnimalTrap;
import com.breakinblocks.horsepowered.blocks.BlockGraniteAnvil;
import com.breakinblocks.horsepowered.blocks.BlockGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockHandGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockPress;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class HorsePowerJadePlugin implements IWailaPlugin {

    public static final Identifier GRINDSTONE = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "grindstone");
    public static final Identifier CHOPPER = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "chopper");
    public static final Identifier PRESS = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "press");
    public static final Identifier MANUAL = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "manual");
    public static final Identifier FILLER = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "filler");
    public static final Identifier DRYING_RACK = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "drying_rack");
    public static final Identifier GRANITE_ANVIL = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "granite_anvil");
    public static final Identifier ANIMAL_TRAP = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "animal_trap");

    private static final String KEY_DR_SLOT = "dr_slot";
    private static final String KEY_DR_PROGRESS = "dr_progress";
    private static final String KEY_DR_TIME = "dr_time";
    private static final String KEY_DR_FINISHED = "dr_finished";

    private static final String KEY_AT_PROGRESS = "at_progress";
    private static final String KEY_AT_TIME = "at_time";
    private static final String KEY_AT_DROP_TIMER = "at_drop_timer";
    private static final String KEY_AT_HAS_ENTITY = "at_has_entity";
    private static final String KEY_AT_ENTITY_ID = "at_entity_id";
    private static final String KEY_AT_BIOME_OK = "at_biome_ok";
    private static final String KEY_AT_WATER_OK = "at_water_ok";
    private static final String KEY_AT_HAS_BAIT = "at_has_bait";

    private static final String KEY_CURRENT = "hp_current";
    private static final String KEY_TOTAL = "hp_total";
    private static final String KEY_HAS_WORKER = "hp_has_worker";
    private static final String KEY_WORKER_NAME = "hp_worker_name";
    private static final String KEY_IS_VALID = "hp_is_valid";
    private static final String KEY_FLUID_NAME = "hp_fluid_name";
    private static final String KEY_FLUID_AMOUNT = "hp_fluid_amount";
    private static final String KEY_FLUID_CAPACITY = "hp_fluid_capacity";
    private static final String KEY_INPUT_FLUID_NAME = "hp_input_fluid_name";
    private static final String KEY_INPUT_FLUID_AMOUNT = "hp_input_fluid_amount";
    private static final String KEY_INPUT_FLUID_CAPACITY = "hp_input_fluid_capacity";
    private static final String KEY_OUTPUT_FLUID_NAME = "hp_output_fluid_name";
    private static final String KEY_OUTPUT_FLUID_AMOUNT = "hp_output_fluid_amount";
    private static final String KEY_OUTPUT_FLUID_CAPACITY = "hp_output_fluid_capacity";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof GrindstoneBlockEntity te) {
                    data.putInt(KEY_CURRENT, te.getCurrentMillTime());
                    data.putInt(KEY_TOTAL, te.getTotalMillTime());
                    data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                    if (te.hasWorkerForDisplay()) {
                        data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName() != null ? te.getWorkerDisplayName() : "Worker");
                    }
                    data.putBoolean(KEY_IS_VALID, te.isValid());
                }
            }

            @Override
            public Identifier getUid() {
                return GRINDSTONE;
            }
        }, GrindstoneBlockEntity.class);

        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof ChopperBlockEntity te) {
                    data.putInt(KEY_CURRENT, te.getCurrentChopTime());
                    data.putInt(KEY_TOTAL, te.getTotalChopTime());
                    data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                    if (te.hasWorkerForDisplay()) {
                        data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName() != null ? te.getWorkerDisplayName() : "Worker");
                    }
                    data.putBoolean(KEY_IS_VALID, te.isValid());
                }
            }

            @Override
            public Identifier getUid() {
                return CHOPPER;
            }
        }, ChopperBlockEntity.class);

        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof PressBlockEntity te) {
                    data.putInt(KEY_CURRENT, te.getCurrentPressStatus());
                    data.putInt(KEY_TOTAL, te.getTotalPressPoints());
                    data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                    if (te.hasWorkerForDisplay()) {
                        data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName() != null ? te.getWorkerDisplayName() : "Worker");
                    }
                    data.putBoolean(KEY_IS_VALID, te.isValid());
                    appendPressTankData(data, te);
                }
            }

            @Override
            public Identifier getUid() {
                return PRESS;
            }
        }, PressBlockEntity.class);

        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                BlockState state = accessor.getBlockState();
                BlockPos pos = accessor.getPosition();
                HitResult hit = accessor.getHitResult();
                if (!(hit instanceof BlockHitResult bhr)) return;
                DryingRackBlockEntity main = BlockDryingRack.getMainBlockEntity(accessor.getLevel(), state, pos);
                if (main == null) return;
                int slot = BlockDryingRack.slotForHit(state, pos, bhr);
                if (slot < 0) return;
                data.putInt(KEY_DR_SLOT, slot);
                data.putInt(KEY_DR_PROGRESS, main.getProgress(slot));
                data.putInt(KEY_DR_TIME, main.getRecipeTime(slot));
                data.putBoolean(KEY_DR_FINISHED, main.isFinished(slot));
            }

            @Override
            public Identifier getUid() {
                return DRYING_RACK;
            }
        }, BlockDryingRack.class);

        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof FillerBlockEntity filler) {
                    HPBlockEntityBase mainTe = filler.getFilledTileEntity();
                    if (mainTe instanceof ChopperBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentChopTime());
                        data.putInt(KEY_TOTAL, te.getTotalChopTime());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.hasWorkerForDisplay()) {
                            data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName() != null ? te.getWorkerDisplayName() : "Worker");
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                    } else if (mainTe instanceof PressBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentPressStatus());
                        data.putInt(KEY_TOTAL, te.getTotalPressPoints());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.hasWorkerForDisplay()) {
                            data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName() != null ? te.getWorkerDisplayName() : "Worker");
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                        appendPressTankData(data, te);
                    } else if (mainTe instanceof GrindstoneBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentMillTime());
                        data.putInt(KEY_TOTAL, te.getTotalMillTime());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.hasWorkerForDisplay()) {
                            data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName() != null ? te.getWorkerDisplayName() : "Worker");
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                    }
                }
            }

            @Override
            public Identifier getUid() {
                return FILLER;
            }
        }, FillerBlockEntity.class);

        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (!(accessor.getBlockEntity() instanceof AnimalTrapBlockEntity te)) return;
                data.putInt(KEY_AT_PROGRESS, te.getTrapProgress());
                data.putInt(KEY_AT_TIME, te.getTrapTime());
                data.putInt(KEY_AT_DROP_TIMER, te.getDropTimer());
                data.putBoolean(KEY_AT_HAS_ENTITY, te.hasCapturedEntity());
                if (te.getCapturedEntityType() != null) {
                    data.putString(KEY_AT_ENTITY_ID,
                            net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                                    .getKey(te.getCapturedEntityType()).toString());
                }
                ItemStack bait = te.getItem(AnimalTrapBlockEntity.BAIT_SLOT);
                data.putBoolean(KEY_AT_HAS_BAIT, !bait.isEmpty());
                if (!bait.isEmpty()) {
                    te.findTrappingRecipe(bait).ifPresent(holder -> {
                        var recipe = holder.value();
                        BlockState state = accessor.getBlockState();
                        boolean biomeOk = recipe.getBiome().isEmpty()
                                || accessor.getLevel().getBiome(accessor.getPosition()).is(recipe.getBiome().get());
                        boolean waterOk = !recipe.isWaterlogged()
                                || (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                                        && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED));
                        data.putBoolean(KEY_AT_BIOME_OK, biomeOk);
                        data.putBoolean(KEY_AT_WATER_OK, waterOk);
                    });
                }
            }

            @Override
            public Identifier getUid() {
                return ANIMAL_TRAP;
            }
        }, AnimalTrapBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof GrindstoneBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                    appendItemInfo(tooltip, te.getItem(2), "secondary");

                    CompoundTag data = accessor.getServerData();
                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public Identifier getUid() {
                return GRINDSTONE;
            }
        }, BlockGrindstone.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof ChopperBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");

                    CompoundTag data = accessor.getServerData();
                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public Identifier getUid() {
                return CHOPPER;
            }
        }, BlockChopper.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof PressBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");

                    CompoundTag data = accessor.getServerData();
                    appendFluidInfoFromData(tooltip, data);

                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public Identifier getUid() {
                return PRESS;
            }
        }, BlockPress.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof FillerBlockEntity filler) {
                    HPBlockEntityBase mainTe = filler.getFilledTileEntity();
                    if (mainTe instanceof ChopperBlockEntity te) {
                        appendItemInfo(tooltip, te.getItem(0), "input");
                        appendItemInfo(tooltip, te.getItem(1), "output");
                    } else if (mainTe instanceof PressBlockEntity te) {
                        appendItemInfo(tooltip, te.getItem(0), "input");
                        appendItemInfo(tooltip, te.getItem(1), "output");
                    } else if (mainTe instanceof GrindstoneBlockEntity te) {
                        appendItemInfo(tooltip, te.getItem(0), "input");
                        appendItemInfo(tooltip, te.getItem(1), "output");
                        appendItemInfo(tooltip, te.getItem(2), "secondary");
                    }

                    CompoundTag data = accessor.getServerData();
                    appendFluidInfoFromData(tooltip, data);
                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public Identifier getUid() {
                return FILLER;
            }
        }, BlockFiller.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockState state = accessor.getBlockState();
                BlockPos pos = accessor.getPosition();
                DryingRackBlockEntity main = BlockDryingRack.getMainBlockEntity(accessor.getLevel(), state, pos);
                if (main == null) return;
                CompoundTag data = accessor.getServerData();
                if (!data.contains(KEY_DR_SLOT)) return;
                int slot = data.getIntOr(KEY_DR_SLOT, -1);
                if (slot < 0) return;
                ItemStack stack = main.getItem(slot);
                if (stack.isEmpty()) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".drying_empty"));
                    return;
                }
                if (data.getBooleanOr(KEY_DR_FINISHED, false)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".drying_done",
                            stack.getHoverName()));
                    return;
                }
                int progress = data.getIntOr(KEY_DR_PROGRESS, 0);
                int time = data.getIntOr(KEY_DR_TIME, 0);
                if (time <= 0) return;
                int percent = (progress * 100) / time;
                int remainingTicks = Math.max(0, time - progress);
                tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".drying",
                        stack.getHoverName(), percent, formatTime(remainingTicks)));
            }

            @Override
            public Identifier getUid() {
                return DRYING_RACK;
            }
        }, BlockDryingRack.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof HandGrindstoneBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                    appendItemInfo(tooltip, te.getItem(2), "secondary");
                } else if (accessor.getBlockEntity() instanceof ManualChopperBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                }
            }

            @Override
            public Identifier getUid() {
                return MANUAL;
            }
        }, BlockHandGrindstone.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof ManualChopperBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                }
            }

            @Override
            public Identifier getUid() {
                return MANUAL;
            }
        }, BlockChoppingBlock.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof GraniteAnvilBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                    if (!te.getItem(0).isEmpty()) {
                        int remaining = Math.max(0, te.getTotalCrushAmount() - te.getCurrentCrushAmount());
                        tooltip.add(Component.translatable(
                                "jade." + HorsePowerMod.MOD_ID + ".strikes_remaining", remaining));
                    }
                }
            }

            @Override
            public Identifier getUid() {
                return GRANITE_ANVIL;
            }
        }, BlockGraniteAnvil.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (!(accessor.getBlockEntity() instanceof AnimalTrapBlockEntity te)) return;
                CompoundTag data = accessor.getServerData();

                for (int slot = 0; slot < AnimalTrapBlockEntity.INVENTORY_SIZE; slot++) {
                    ItemStack stack = te.getItem(slot);
                    String key = slot == AnimalTrapBlockEntity.BAIT_SLOT ? "input" : "output";
                    appendItemInfo(tooltip, stack, key);
                }

                if (data.getBooleanOr(KEY_AT_HAS_ENTITY, false)) {
                    Component entityName = Component.literal("?");
                    String id = data.getStringOr(KEY_AT_ENTITY_ID, "");
                    if (!id.isEmpty()) {
                        Identifier rl = Identifier.tryParse(id);
                        if (rl != null) {
                            net.minecraft.world.entity.EntityType<?> type =
                                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(rl);
                            if (type != null) entityName = Component.translatable(type.getDescriptionId());
                        }
                    }
                    tooltip.add(Component.translatable(
                            "jade." + HorsePowerMod.MOD_ID + ".trap_caught", entityName));
                    int nextDropTicks = Math.max(0,
                            AnimalTrapBlockEntity.DROP_INTERVAL_TICKS - data.getIntOr(KEY_AT_DROP_TIMER, 0));
                    tooltip.add(Component.translatable(
                            "jade." + HorsePowerMod.MOD_ID + ".trap_next_drop", formatTime(nextDropTicks)));
                    return;
                }

                if (!data.getBooleanOr(KEY_AT_HAS_BAIT, false)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_empty"));
                    return;
                }

                int progress = data.getIntOr(KEY_AT_PROGRESS, 0);
                int time = data.getIntOr(KEY_AT_TIME, 0);
                if (time > 0 && progress < time) {
                    int remaining = Math.max(0, time - progress);
                    tooltip.add(Component.translatable(
                            "jade." + HorsePowerMod.MOD_ID + ".trap_progress", formatTime(remaining)));
                } else {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_set"));
                }

                if (data.contains(KEY_AT_BIOME_OK) && !data.getBooleanOr(KEY_AT_BIOME_OK, true)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_wrong_biome")
                            .withStyle(style -> style.withColor(0xFF5555)));
                }
                if (data.contains(KEY_AT_WATER_OK) && !data.getBooleanOr(KEY_AT_WATER_OK, true)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_needs_water")
                            .withStyle(style -> style.withColor(0xFF5555)));
                }
            }

            @Override
            public Identifier getUid() {
                return ANIMAL_TRAP;
            }
        }, BlockAnimalTrap.class);
    }

    private static String formatTime(int ticks) {
        int totalSeconds = ticks / 20;
        if (totalSeconds < 60) return totalSeconds + "s";
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (seconds == 0) return minutes + "m";
        return minutes + "m " + seconds + "s";
    }

    private static void appendItemInfo(ITooltip tooltip, ItemStack stack, String type) {
        if (!stack.isEmpty()) {
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + "." + type,
                    stack.getHoverName(), stack.getCount()));
        }
    }

    private static void appendFluidInfoFromData(ITooltip tooltip, CompoundTag data) {
        data.getString(KEY_INPUT_FLUID_NAME).ifPresent(fluidName -> {
            int amount = data.getInt(KEY_INPUT_FLUID_AMOUNT).orElse(0);
            int capacity = data.getInt(KEY_INPUT_FLUID_CAPACITY).orElse(0);
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".fluid_input",
                    fluidName, amount, capacity));
        });
        data.getString(KEY_OUTPUT_FLUID_NAME).ifPresent(fluidName -> {
            int amount = data.getInt(KEY_OUTPUT_FLUID_AMOUNT).orElse(0);
            int capacity = data.getInt(KEY_OUTPUT_FLUID_CAPACITY).orElse(0);
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".fluid_output",
                    fluidName, amount, capacity));
        });
        // Legacy single-tank key — kept for save-format compatibility during migration.
        data.getString(KEY_FLUID_NAME).ifPresent(fluidName -> {
            int amount = data.getInt(KEY_FLUID_AMOUNT).orElse(0);
            int capacity = data.getInt(KEY_FLUID_CAPACITY).orElse(0);
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".fluid",
                    fluidName, amount, capacity));
        });
    }

    private static void appendPressTankData(CompoundTag data, PressBlockEntity te) {
        int capacity = te.getTankCapacity();
        FluidStack inputFluid = te.getInputFluid();
        if (!inputFluid.isEmpty()) {
            data.putString(KEY_INPUT_FLUID_NAME, inputFluid.getHoverName().getString());
            data.putInt(KEY_INPUT_FLUID_AMOUNT, inputFluid.getAmount());
            data.putInt(KEY_INPUT_FLUID_CAPACITY, capacity);
        }
        FluidStack outputFluid = te.getOutputFluid();
        if (!outputFluid.isEmpty()) {
            data.putString(KEY_OUTPUT_FLUID_NAME, outputFluid.getHoverName().getString());
            data.putInt(KEY_OUTPUT_FLUID_AMOUNT, outputFluid.getAmount());
            data.putInt(KEY_OUTPUT_FLUID_CAPACITY, capacity);
        }
    }

    private static void appendProgressFromData(ITooltip tooltip, CompoundTag data) {
        data.getInt(KEY_TOTAL).ifPresent(total -> {
            if (total > 0) {
                int current = data.getInt(KEY_CURRENT).orElse(0);
                int percent = (current * 100) / total;
                tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".progress", percent));
            }
        });
    }

    private static void appendWorkerInfoFromData(ITooltip tooltip, CompoundTag data) {
        data.getBoolean(KEY_HAS_WORKER).ifPresent(hasWorker -> {
            if (hasWorker) {
                data.getString(KEY_WORKER_NAME).ifPresentOrElse(
                        workerName -> {
                            String name = workerName.isEmpty() ? "Unknown" : workerName;
                            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".worker", name));
                        },
                        () -> tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".worker_attached"))
                );
            } else {
                tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".no_worker"));
            }
        });

        data.getBoolean(KEY_IS_VALID).ifPresent(isValid -> {
            if (!isValid) {
                tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".obstructed")
                        .withStyle(style -> style.withColor(0xFF5555)));
            }
        });
    }
}
