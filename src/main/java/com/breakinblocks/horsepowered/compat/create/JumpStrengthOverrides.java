package com.breakinblocks.horsepowered.compat.create;

import com.breakinblocks.horsepowered.lib.Reference;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JumpStrengthOverrides extends SimpleJsonResourceReloadListener {

    public static final String FOLDER = "horse_engine_jump_strength";
    private static final Logger LOGGER = LoggerFactory.getLogger(JumpStrengthOverrides.class);
    private static final Gson GSON = new Gson();

    private static volatile Map<ResourceLocation, Double> byId = Map.of();
    private static volatile List<Map.Entry<TagKey<EntityType<?>>, Double>> byTag = List.of();

    public JumpStrengthOverrides() {
        super(GSON, FOLDER);
    }

    public static Double lookup(EntityType<?> type, ResourceLocation id) {
        Double direct = byId.get(id);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<TagKey<EntityType<?>>, Double> entry : byTag) {
            if (type.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Double> ids = new HashMap<>();
        List<Map.Entry<TagKey<EntityType<?>>, Double>> tags = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> file : files.entrySet()) {
            try {
                JsonObject root = GsonHelper.convertToJsonObject(file.getValue(), "root");
                if (!root.has("values")) {
                    continue;
                }
                JsonObject values = GsonHelper.getAsJsonObject(root, "values");
                for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                    String key = entry.getKey();
                    double value = GsonHelper.convertToDouble(entry.getValue(), key);
                    if (key.startsWith("#")) {
                        ResourceLocation tagId = new ResourceLocation(key.substring(1));
                        tags.add(Map.entry(TagKey.create(Registries.ENTITY_TYPE, tagId), value));
                    } else {
                        ids.put(new ResourceLocation(key), value);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[{}] Could not read jump strength overrides from {}", Reference.MODID, file.getKey(), e);
            }
        }

        byId = Map.copyOf(ids);
        byTag = List.copyOf(tags);
    }
}
