package com.breakinblocks.horsepowered.compat.jade;

import com.breakinblocks.horsepowered.blockentity.AnimalTrapBlockEntity;
import com.breakinblocks.horsepowered.blockentity.ChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.DryingRackBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GraniteAnvilBlockEntity;
import com.breakinblocks.horsepowered.blockentity.GrindstoneBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HandGrindstoneBlockEntity;
import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.breakinblocks.horsepowered.blockentity.VirtualWorker;
import com.breakinblocks.horsepowered.blockentity.WorkerHost;
import com.breakinblocks.horsepowered.blockentity.ManualChopperBlockEntity;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.blocks.BlockAnimalTrap;
import com.breakinblocks.horsepowered.blocks.BlockChopper;
import com.breakinblocks.horsepowered.blocks.BlockChoppingBlock;
import com.breakinblocks.horsepowered.blocks.BlockDryingRack;
import com.breakinblocks.horsepowered.blocks.BlockGraniteAnvil;
import com.breakinblocks.horsepowered.blocks.BlockGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockHandGrindstone;
import com.breakinblocks.horsepowered.blocks.BlockPress;
import com.breakinblocks.horsepowered.compat.create.jade.HorseEngineJadeSupport;
import com.breakinblocks.horsepowered.lib.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
    public static final ResourceLocation GRANITE_ANVIL = new ResourceLocation(Reference.MODID, "granite_anvil");
    public static final ResourceLocation ANIMAL_TRAP = new ResourceLocation(Reference.MODID, "animal_trap");

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

    @Override
    public void register(IWailaCommonRegistration registration) {
        if (HorsePowerMod.CREATE_LOADED) {
            HorseEngineJadeSupport.register(registration);
        }
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
                    te.findTrappingRecipe(bait).ifPresent(recipe -> {
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

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getBlockEntity() instanceof GraniteAnvilBlockEntity te) {
                    appendItemInfo(tooltip, te.getItem(0), "input");
                    appendItemInfo(tooltip, te.getItem(1), "output");
                    if (!te.getItem(0).isEmpty()) {
                        int remaining = Math.max(0, te.getTotalCrushAmount() - te.getCurrentCrushAmount());
                        tooltip.add(Component.translatable(
                                "jade." + Reference.MODID + ".strikes_remaining", remaining));
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
                    ItemStack stack = te.getItem(slot);
                    String key = slot == AnimalTrapBlockEntity.BAIT_SLOT ? "input" : "output";
                    appendItemInfo(tooltip, stack, key);
                }

                if (data.getBoolean(KEY_AT_HAS_ENTITY)) {
                    Component entityName = Component.literal("?");
                    String id = data.getString(KEY_AT_ENTITY_ID);
                    if (!id.isEmpty()) {
                        ResourceLocation rl = ResourceLocation.tryParse(id);
                        if (rl != null) {
                            net.minecraft.world.entity.EntityType<?> type =
                                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(rl);
                            if (type != null) entityName = Component.translatable(type.getDescriptionId());
                        }
                    }
                    tooltip.add(Component.translatable(
                            "jade." + Reference.MODID + ".trap_caught", entityName));
                    int nextDropTicks = Math.max(0,
                            AnimalTrapBlockEntity.DROP_INTERVAL_TICKS - data.getInt(KEY_AT_DROP_TIMER));
                    tooltip.add(Component.translatable(
                            "jade." + Reference.MODID + ".trap_next_drop", formatTime(nextDropTicks)));
                    return;
                }

                if (!data.getBoolean(KEY_AT_HAS_BAIT)) {
                    tooltip.add(Component.translatable("jade." + Reference.MODID + ".trap_empty"));
                    return;
                }

                int progress = data.getInt(KEY_AT_PROGRESS);
                int time = data.getInt(KEY_AT_TIME);
                if (time > 0 && progress < time) {
                    int remaining = Math.max(0, time - progress);
                    tooltip.add(Component.translatable(
                            "jade." + Reference.MODID + ".trap_progress", formatTime(remaining)));
                } else {
                    tooltip.add(Component.translatable("jade." + Reference.MODID + ".trap_set"));
                }

                if (data.contains(KEY_AT_BIOME_OK) && !data.getBoolean(KEY_AT_BIOME_OK)) {
                    tooltip.add(Component.translatable("jade." + Reference.MODID + ".trap_wrong_biome")
                            .withStyle(style -> style.withColor(0xFF5555)));
                }
                if (data.contains(KEY_AT_WATER_OK) && !data.getBoolean(KEY_AT_WATER_OK)) {
                    tooltip.add(Component.translatable("jade." + Reference.MODID + ".trap_needs_water")
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

    public static void appendWorkerInfo(ITooltip tooltip, WorkerHost host) {
        VirtualWorker te = host.getVirtualWorker();
        if (te.hasWorker()) {
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
