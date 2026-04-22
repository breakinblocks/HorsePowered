package com.breakinblocks.horsepowered.compat.jade;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.FillerBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityBase;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.blocks.BlockChoppingBlock;
import com.breakinblocks.horsepowered.blocks.BlockFiller;
import com.breakinblocks.horsepowered.blocks.BlockGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockHandGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockPress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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

    // NBT keys for server data
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
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
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

    private static void appendWorkerInfoFromData(ITooltip tooltip, CompoundTag data) {
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
