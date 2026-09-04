package com.breakinblocks.horsepowered.compat.jade;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blockentity.AnimalTrapBlockEntity;
import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GraniteAnvilBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityBase;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockAnimalTrap;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.blocks.BlockChoppingBlock;
import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.breakinblocks.horsepowered.blocks.BlockFiller;
import com.breakinblocks.horsepowered.blocks.BlockGraniteAnvil;
import com.breakinblocks.horsepowered.blocks.BlockGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockHandGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockPress;
import com.breakinblocks.horsepowered.compat.create.jade.HorseEngineJadeSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

    public static final ResourceLocation GRINDSTONE = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "grindstone");
    public static final ResourceLocation CHOPPER = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "chopper");
    public static final ResourceLocation PRESS = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "press");
    public static final ResourceLocation MANUAL = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "manual");
    public static final ResourceLocation FILLER = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "filler");
    public static final ResourceLocation DRYING_RACK = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "drying_rack");
    public static final ResourceLocation GRANITE_ANVIL = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "granite_anvil");
    public static final ResourceLocation ANIMAL_TRAP = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "animal_trap");

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

    // NBT keys for server data
    private static final String KEY_CURRENT = "hp_current";
    private static final String KEY_TOTAL = "hp_total";
    public static final String KEY_HAS_WORKER = "hp_has_worker";
    public static final String KEY_WORKER_NAME = "hp_worker_name";
    public static final String KEY_IS_VALID = "hp_is_valid";
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
        if (HorsePowerMod.CREATE_LOADED) {
            HorseEngineJadeSupport.register(registration);
        }
        // Register server data providers to sync progress from server
        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof GrindstoneBlockEntity te) {
                    data.putInt(KEY_CURRENT, te.getCurrentMillTime());
                    data.putInt(KEY_TOTAL, te.getTotalMillTime());
                    data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                    if (te.getWorkerDisplayName() != null) {
                        data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName());
                    }
                    data.putBoolean(KEY_IS_VALID, te.isValid());
                }
            }

            @Override
            public ResourceLocation getUid() {
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
                    if (te.getWorkerDisplayName() != null) {
                        data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName());
                    }
                    data.putBoolean(KEY_IS_VALID, te.isValid());
                }
            }

            @Override
            public ResourceLocation getUid() {
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
                    if (te.getWorkerDisplayName() != null) {
                        data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName());
                    }
                    data.putBoolean(KEY_IS_VALID, te.isValid());
                    appendPressTankData(data, te);
                }
            }

            @Override
            public ResourceLocation getUid() {
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
            public ResourceLocation getUid() {
                return DRYING_RACK;
            }
        }, BlockDryingRack.class);

        // Server data provider for filler blocks - get data from the main block
        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof FillerBlockEntity filler) {
                    HPBlockEntityBase mainTe = filler.getFilledTileEntity();
                    if (mainTe instanceof ChopperBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentChopTime());
                        data.putInt(KEY_TOTAL, te.getTotalChopTime());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.getWorkerDisplayName() != null) {
                            data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName());
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                    } else if (mainTe instanceof PressBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentPressStatus());
                        data.putInt(KEY_TOTAL, te.getTotalPressPoints());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.getWorkerDisplayName() != null) {
                            data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName());
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                        appendPressTankData(data, te);
                    } else if (mainTe instanceof GrindstoneBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentMillTime());
                        data.putInt(KEY_TOTAL, te.getTotalMillTime());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.getWorkerDisplayName() != null) {
                            data.putString(KEY_WORKER_NAME, te.getWorkerDisplayName());
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                    }
                }
            }

            @Override
            public ResourceLocation getUid() {
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
                            BuiltInRegistries.ENTITY_TYPE.getKey(te.getCapturedEntityType()).toString());
                }
                ItemStack bait = te.getInventory().getStackInSlot(AnimalTrapBlockEntity.BAIT_SLOT);
                data.putBoolean(KEY_AT_HAS_BAIT, !bait.isEmpty());
                if (!bait.isEmpty()) {
                    te.findRecipe(bait).ifPresent(holder -> {
                        var recipe = holder.value();
                        BlockState state = accessor.getBlockState();
                        boolean biomeOk = recipe.getBiome().isEmpty()
                                || accessor.getLevel().getBiome(accessor.getPosition()).is(recipe.getBiome().get());
                        boolean waterOk = !recipe.isWaterlogged()
                                || (state.hasProperty(BlockStateProperties.WATERLOGGED)
                                        && state.getValue(BlockStateProperties.WATERLOGGED));
                        data.putBoolean(KEY_AT_BIOME_OK, biomeOk);
                        data.putBoolean(KEY_AT_WATER_OK, waterOk);
                    });
                }
            }

            @Override
            public ResourceLocation getUid() {
                return ANIMAL_TRAP;
            }
        }, AnimalTrapBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (HorsePowerMod.CREATE_LOADED) {
            HorseEngineJadeSupport.registerClient(registration);
        }
        // Horse-powered grindstone
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof GrindstoneBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                    appendItemInfo(tooltip, te.getItem(2), "secondary");

                    // Use server data for progress
                    CompoundTag data = accessor.getServerData();
                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return GRINDSTONE;
            }
        }, BlockGrindstone.class);

        // Horse-powered chopper
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof ChopperBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");

                    // Use server data for progress
                    CompoundTag data = accessor.getServerData();
                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return CHOPPER;
            }
        }, BlockChopper.class);

        // Horse-powered press
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof PressBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");

                    // Use server data for fluid info
                    CompoundTag data = accessor.getServerData();
                    appendFluidInfoFromData(tooltip, data);

                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return PRESS;
            }
        }, BlockPress.class);

        // Filler block - show info from the main block
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

                    // Use server data for progress
                    CompoundTag data = accessor.getServerData();
                    appendFluidInfoFromData(tooltip, data);
                    appendProgressFromData(tooltip, data);
                    appendWorkerInfoFromData(tooltip, data);
                }
            }

            @Override
            public ResourceLocation getUid() {
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

                int slot = data.getInt(KEY_DR_SLOT);
                ItemStack stack = main.getItem(slot);
                if (stack.isEmpty()) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".drying_empty"));
                    return;
                }

                if (data.getBoolean(KEY_DR_FINISHED)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".drying_done",
                            stack.getHoverName()));
                    return;
                }

                int progress = data.getInt(KEY_DR_PROGRESS);
                int time = data.getInt(KEY_DR_TIME);
                if (time <= 0) return;
                int percent = (progress * 100) / time;
                int remainingTicks = Math.max(0, time - progress);
                String remaining = formatTime(remainingTicks);
                tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".drying",
                        stack.getHoverName(), percent, remaining));
            }

            @Override
            public ResourceLocation getUid() {
                return DRYING_RACK;
            }
        }, BlockDryingRack.class);

        // Manual blocks (hand grindstone and chopping block)
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
            public ResourceLocation getUid() {
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
            public ResourceLocation getUid() {
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
            public ResourceLocation getUid() {
                return GRANITE_ANVIL;
            }
        }, BlockGraniteAnvil.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (!(accessor.getBlockEntity() instanceof AnimalTrapBlockEntity te)) return;
                CompoundTag data = accessor.getServerData();

                for (int slot = 0; slot < AnimalTrapBlockEntity.INVENTORY_SIZE; slot++) {
                    ItemStack stack = te.getInventory().getStackInSlot(slot);
                    String key = slot == AnimalTrapBlockEntity.BAIT_SLOT ? "input" : "output";
                    appendItemInfo(tooltip, stack, key);
                }

                if (data.getBoolean(KEY_AT_HAS_ENTITY)) {
                    Component entityName = Component.literal("?");
                    String id = data.getString(KEY_AT_ENTITY_ID);
                    if (!id.isEmpty()) {
                        ResourceLocation rl = ResourceLocation.tryParse(id);
                        if (rl != null) {
                            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl);
                            if (type != null) entityName = Component.translatable(type.getDescriptionId());
                        }
                    }
                    tooltip.add(Component.translatable(
                            "jade." + HorsePowerMod.MOD_ID + ".trap_caught", entityName));
                    int nextDropTicks = Math.max(0,
                            AnimalTrapBlockEntity.DROP_INTERVAL_TICKS - data.getInt(KEY_AT_DROP_TIMER));
                    tooltip.add(Component.translatable(
                            "jade." + HorsePowerMod.MOD_ID + ".trap_next_drop", formatTime(nextDropTicks)));
                    return;
                }

                if (!data.getBoolean(KEY_AT_HAS_BAIT)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_empty"));
                    return;
                }

                int progress = data.getInt(KEY_AT_PROGRESS);
                int time = data.getInt(KEY_AT_TIME);
                if (time > 0 && progress < time) {
                    int remaining = Math.max(0, time - progress);
                    tooltip.add(Component.translatable(
                            "jade." + HorsePowerMod.MOD_ID + ".trap_progress", formatTime(remaining)));
                } else {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_set"));
                }

                if (data.contains(KEY_AT_BIOME_OK) && !data.getBoolean(KEY_AT_BIOME_OK)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_wrong_biome")
                            .withStyle(style -> style.withColor(0xFF5555)));
                }
                if (data.contains(KEY_AT_WATER_OK) && !data.getBoolean(KEY_AT_WATER_OK)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".trap_needs_water")
                            .withStyle(style -> style.withColor(0xFF5555)));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return ANIMAL_TRAP;
            }
        }, BlockAnimalTrap.class);

    }

    private static String formatTime(int ticks) {
        int totalSeconds = ticks / 20;
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        }
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
        if (data.contains(KEY_INPUT_FLUID_NAME)) {
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".fluid_input",
                    data.getString(KEY_INPUT_FLUID_NAME),
                    data.getInt(KEY_INPUT_FLUID_AMOUNT),
                    data.getInt(KEY_INPUT_FLUID_CAPACITY)));
        }
        if (data.contains(KEY_OUTPUT_FLUID_NAME)) {
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".fluid_output",
                    data.getString(KEY_OUTPUT_FLUID_NAME),
                    data.getInt(KEY_OUTPUT_FLUID_AMOUNT),
                    data.getInt(KEY_OUTPUT_FLUID_CAPACITY)));
        }
        // Legacy single-tank key — kept for save-format compatibility during migration.
        if (data.contains(KEY_FLUID_NAME)) {
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".fluid",
                    data.getString(KEY_FLUID_NAME),
                    data.getInt(KEY_FLUID_AMOUNT),
                    data.getInt(KEY_FLUID_CAPACITY)));
        }
    }

    private static void appendPressTankData(CompoundTag data, PressBlockEntity te) {
        FluidStack inputFluid = te.getInputTank().getFluid();
        if (!inputFluid.isEmpty()) {
            data.putString(KEY_INPUT_FLUID_NAME, inputFluid.getHoverName().getString());
            data.putInt(KEY_INPUT_FLUID_AMOUNT, inputFluid.getAmount());
            data.putInt(KEY_INPUT_FLUID_CAPACITY, te.getInputTank().getCapacity());
        }
        FluidStack outputFluid = te.getOutputTank().getFluid();
        if (!outputFluid.isEmpty()) {
            data.putString(KEY_OUTPUT_FLUID_NAME, outputFluid.getHoverName().getString());
            data.putInt(KEY_OUTPUT_FLUID_AMOUNT, outputFluid.getAmount());
            data.putInt(KEY_OUTPUT_FLUID_CAPACITY, te.getOutputTank().getCapacity());
        }
    }

    private static void appendProgressFromData(ITooltip tooltip, CompoundTag data) {
        if (data.contains(KEY_TOTAL)) {
            int total = data.getInt(KEY_TOTAL);
            if (total > 0) {
                int current = data.getInt(KEY_CURRENT);
                int percent = (current * 100) / total;
                tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".progress", percent));
            }
        }
    }

    public static void appendWorkerInfoFromData(ITooltip tooltip, CompoundTag data) {
        if (data.contains(KEY_HAS_WORKER)) {
            if (data.getBoolean(KEY_HAS_WORKER)) {
                if (data.contains(KEY_WORKER_NAME)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".worker",
                            data.getString(KEY_WORKER_NAME)));
                } else {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".worker_attached"));
                }
            } else {
                tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".no_worker"));
            }
        }

        if (data.contains(KEY_IS_VALID) && !data.getBoolean(KEY_IS_VALID)) {
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".obstructed")
                    .withStyle(style -> style.withColor(0xFF5555)));
        }
    }
}
