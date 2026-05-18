package com.breakinblocks.horsepowered.compat.jade;

import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.blocks.BlockChoppingBlock;
import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.breakinblocks.horsepowered.blocks.BlockGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockHandGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockPress;
import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
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

    public static final ResourceLocation GRINDSTONE = new ResourceLocation(Reference.MODID, "grindstone");
    public static final ResourceLocation CHOPPER = new ResourceLocation(Reference.MODID, "chopper");
    public static final ResourceLocation PRESS = new ResourceLocation(Reference.MODID, "press");
    public static final ResourceLocation MANUAL = new ResourceLocation(Reference.MODID, "manual");
    public static final ResourceLocation DRYING_RACK = new ResourceLocation(Reference.MODID, "drying_rack");

    private static final String KEY_DR_SLOT = "dr_slot";
    private static final String KEY_DR_PROGRESS = "dr_progress";
    private static final String KEY_DR_TIME = "dr_time";
    private static final String KEY_DR_FINISHED = "dr_finished";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                BlockState state = accessor.getBlockState();
                BlockPos pos = accessor.getPosition();
                DryingRackBlockEntity main = BlockDryingRack.getMainBlockEntity(accessor.getLevel(), state, pos);
                if (main == null) return;
                BlockHitResult hit = accessor.getHitResult();
                int slot = BlockDryingRack.slotForHit(state, pos, hit);
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
        }, DryingRackBlockEntity.class);
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
                    appendProgress(tooltip, te.getCurrentMillTime(), te.getTotalMillTime());
                    appendWorkerInfo(tooltip, te);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return GRINDSTONE;
            }
        }, BlockGrindstone.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof ChopperBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                    appendProgress(tooltip, te.getCurrentChopTime(), te.getTotalChopTime());
                    appendWorkerInfo(tooltip, te);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return CHOPPER;
            }
        }, BlockChopper.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof PressBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                    appendFluidInfo(tooltip, te.getInputTank().getFluid(), te.getInputTank().getCapacity(), "fluid_input");
                    appendFluidInfo(tooltip, te.getOutputTank().getFluid(), te.getOutputTank().getCapacity(), "fluid_output");
                    appendWorkerInfo(tooltip, te);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return PRESS;
            }
        }, BlockPress.class);

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
                BlockState state = accessor.getBlockState();
                BlockPos pos = accessor.getPosition();
                DryingRackBlockEntity main = BlockDryingRack.getMainBlockEntity(accessor.getLevel(), state, pos);
                if (main == null) return;

                CompoundTag data = accessor.getServerData();
                if (!data.contains(KEY_DR_SLOT)) return;

                int slot = data.getInt(KEY_DR_SLOT);
                ItemStack stack = main.getItem(slot);
                if (stack.isEmpty()) {
                    tooltip.add(Component.translatable("jade." + Reference.MODID + ".drying_empty"));
                    return;
                }

                if (data.getBoolean(KEY_DR_FINISHED)) {
                    tooltip.add(Component.translatable("jade." + Reference.MODID + ".drying_done",
                            stack.getHoverName()));
                    return;
                }

                int progress = data.getInt(KEY_DR_PROGRESS);
                int time = data.getInt(KEY_DR_TIME);
                if (time <= 0) return;
                int percent = (progress * 100) / time;
                int remainingTicks = Math.max(0, time - progress);
                String remaining = formatTime(remainingTicks);
                tooltip.add(Component.translatable("jade." + Reference.MODID + ".drying",
                        stack.getHoverName(), percent, remaining));
            }

            @Override
            public ResourceLocation getUid() {
                return DRYING_RACK;
            }
        }, BlockDryingRack.class);
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
            tooltip.add(Component.translatable("jade." + Reference.MODID + "." + type,
                    stack.getHoverName(), stack.getCount()));
        }
    }

    private static void appendFluidInfo(ITooltip tooltip, FluidStack fluid, int capacity, String key) {
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("jade." + Reference.MODID + "." + key,
                    fluid.getDisplayName(), fluid.getAmount(), capacity));
        }
    }

    private static void appendProgress(ITooltip tooltip, int current, int total) {
        if (total > 0 && current > 0) {
            int percent = (current * 100) / total;
            tooltip.add(Component.translatable("jade." + Reference.MODID + ".progress", percent));
        }
    }

    private static void appendWorkerInfo(ITooltip tooltip, HPBlockEntityHorseBase te) {
        if (te.hasWorkerForDisplay()) {
            String name = te.getWorkerDisplayName();
            if (name != null && !name.isEmpty()) {
                tooltip.add(Component.translatable("jade." + Reference.MODID + ".worker",
                        Component.literal(name)));
            } else {
                tooltip.add(Component.translatable("jade." + Reference.MODID + ".worker_attached"));
            }
        } else {
            tooltip.add(Component.translatable("jade." + Reference.MODID + ".no_worker"));
        }

        if (!te.isValid()) {
            tooltip.add(Component.translatable("jade." + Reference.MODID + ".obstructed")
                    .withStyle(style -> style.withColor(0xFF5555)));
        }
    }
}
