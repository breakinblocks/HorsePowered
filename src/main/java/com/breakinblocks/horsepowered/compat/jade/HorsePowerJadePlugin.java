package com.breakinblocks.horsepowered.compat.jade;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.*;
import com.breakinblocks.horsepowered.blockentity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class HorsePowerJadePlugin implements IWailaPlugin {

    public static final Identifier GRINDSTONE = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "grindstone");
    public static final Identifier CHOPPER = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "chopper");
    public static final Identifier PRESS = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "press");
    public static final Identifier MANUAL = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "manual");
    public static final Identifier FILLER = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "filler");

    // NBT keys for server data
    private static final String KEY_CURRENT = "hp_current";
    private static final String KEY_TOTAL = "hp_total";
    private static final String KEY_HAS_WORKER = "hp_has_worker";
    private static final String KEY_WORKER_NAME = "hp_worker_name";
    private static final String KEY_IS_VALID = "hp_is_valid";
    private static final String KEY_FLUID_NAME = "hp_fluid_name";
    private static final String KEY_FLUID_AMOUNT = "hp_fluid_amount";
    private static final String KEY_FLUID_CAPACITY = "hp_fluid_capacity";

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
                    if (te.getWorker() != null) {
                        data.putString(KEY_WORKER_NAME, te.getWorker().getDisplayName().getString());
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
                    if (te.getWorker() != null) {
                        data.putString(KEY_WORKER_NAME, te.getWorker().getDisplayName().getString());
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
                    if (te.getWorker() != null) {
                        data.putString(KEY_WORKER_NAME, te.getWorker().getDisplayName().getString());
                    }
                    data.putBoolean(KEY_IS_VALID, te.isValid());
                    FluidStack fluid = te.getTank().getFluid();
                    if (!fluid.isEmpty()) {
                        data.putString(KEY_FLUID_NAME, fluid.getHoverName().getString());
                        data.putInt(KEY_FLUID_AMOUNT, fluid.getAmount());
                        data.putInt(KEY_FLUID_CAPACITY, te.getTank().getCapacity());
                    }
                }
            }

            @Override
            public Identifier getUid() {
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
                        if (te.getWorker() != null) {
                            data.putString(KEY_WORKER_NAME, te.getWorker().getDisplayName().getString());
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                    } else if (mainTe instanceof PressBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentPressStatus());
                        data.putInt(KEY_TOTAL, te.getTotalPressPoints());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.getWorker() != null) {
                            data.putString(KEY_WORKER_NAME, te.getWorker().getDisplayName().getString());
                        }
                        data.putBoolean(KEY_IS_VALID, te.isValid());
                        FluidStack fluid = te.getTank().getFluid();
                        if (!fluid.isEmpty()) {
                            data.putString(KEY_FLUID_NAME, fluid.getHoverName().getString());
                            data.putInt(KEY_FLUID_AMOUNT, fluid.getAmount());
                            data.putInt(KEY_FLUID_CAPACITY, te.getTank().getCapacity());
                        }
                    } else if (mainTe instanceof GrindstoneBlockEntity te) {
                        data.putInt(KEY_CURRENT, te.getCurrentMillTime());
                        data.putInt(KEY_TOTAL, te.getTotalMillTime());
                        data.putBoolean(KEY_HAS_WORKER, te.hasWorkerForDisplay());
                        if (te.getWorker() != null) {
                            data.putString(KEY_WORKER_NAME, te.getWorker().getDisplayName().getString());
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
            public Identifier getUid() {
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
            public Identifier getUid() {
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
            public Identifier getUid() {
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
            public Identifier getUid() {
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
    }

    private static void appendItemInfo(ITooltip tooltip, ItemStack stack, String type) {
        if (!stack.isEmpty()) {
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + "." + type,
                    stack.getHoverName(), stack.getCount()));
        }
    }

    private static void appendFluidInfoFromData(ITooltip tooltip, CompoundTag data) {
        data.getString(KEY_FLUID_NAME).ifPresent(fluidName -> {
            int amount = data.getInt(KEY_FLUID_AMOUNT).orElse(0);
            int capacity = data.getInt(KEY_FLUID_CAPACITY).orElse(0);
            tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".fluid",
                    fluidName, amount, capacity));
        });
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
                        workerName -> tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".worker", workerName)),
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
