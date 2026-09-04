package com.breakinblocks.horsepowered.compat.create.jade;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.compat.create.HorseEngineBlock;
import com.breakinblocks.horsepowered.compat.create.HorseEngineBlockEntity;
import com.breakinblocks.horsepowered.compat.jade.HorsePowerJadePlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.config.IPluginConfig;

public final class HorseEngineJadeSupport {

    public static final ResourceLocation HORSE_ENGINE = HorsePowerMod.id("horse_engine");

    private static final String KEY_STRESS = "hp_stress";
    private static final String KEY_RPM = "hp_rpm";
    private static final String KEY_JUMP = "hp_jump";

    private HorseEngineJadeSupport() {
    }

    public static void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof HorseEngineBlockEntity te) {
                    data.putBoolean(HorsePowerJadePlugin.KEY_HAS_WORKER, te.getVirtualWorker().hasWorker());
                    String name = te.getVirtualWorker().getWorkerDisplayName();
                    if (name != null) {
                        data.putString(HorsePowerJadePlugin.KEY_WORKER_NAME, name);
                    }
                    data.putBoolean(HorsePowerJadePlugin.KEY_IS_VALID, te.getVirtualWorker().isValid());
                    data.putFloat(KEY_STRESS, te.getStressOutput());
                    data.putFloat(KEY_RPM, Math.abs(te.getGeneratedSpeed()));
                    data.putDouble(KEY_JUMP, te.getJumpStrength());
                }
            }

            @Override
            public ResourceLocation getUid() {
                return HORSE_ENGINE;
            }
        }, HorseEngineBlockEntity.class);
    }

    public static void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (!(accessor.getBlockEntity() instanceof HorseEngineBlockEntity)) {
                    return;
                }
                CompoundTag data = accessor.getServerData();
                HorsePowerJadePlugin.appendWorkerInfoFromData(tooltip, data);
                if (data.contains(KEY_JUMP) && data.getBoolean(HorsePowerJadePlugin.KEY_HAS_WORKER)) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".jump_strength",
                            String.format("%.2f", data.getDouble(KEY_JUMP))));
                }
                if (data.contains(KEY_STRESS) && data.getFloat(KEY_RPM) > 0) {
                    tooltip.add(Component.translatable("jade." + HorsePowerMod.MOD_ID + ".stress_output",
                            String.format("%.1f", data.getFloat(KEY_STRESS)),
                            String.format("%.0f", data.getFloat(KEY_RPM))));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return HORSE_ENGINE;
            }
        }, HorseEngineBlock.class);
    }
}
