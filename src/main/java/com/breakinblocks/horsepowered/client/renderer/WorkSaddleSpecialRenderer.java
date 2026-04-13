package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class WorkSaddleSpecialRenderer implements SpecialModelRenderer<Entity> {

    private static final int CACHE_LIMIT = 32;
    private static final Map<TypedEntityData<EntityType<?>>, Entity> ENTITY_CACHE =
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<TypedEntityData<EntityType<?>>, Entity> eldest) {
                    return size() > CACHE_LIMIT;
                }
            };

    @Override
    public @Nullable Entity extractArgument(ItemStack stack) {
        TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
        if (data == null) return null;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        Entity cached = ENTITY_CACHE.get(data);
        if (cached != null) return cached;
        Entity created = createEntity(level, data);
        if (created != null) {
            ENTITY_CACHE.put(data, created);
        }
        return created;
    }

    private static @Nullable Entity createEntity(ClientLevel level, TypedEntityData<EntityType<?>> data) {
        try {
            Entity entity = data.type().create(level, EntitySpawnReason.LOAD);
            if (entity == null) return null;
            data.loadInto(entity);
            entity.setNoGravity(true);
            entity.setSilent(true);
            if (entity instanceof LivingEntity living) {
                living.yBodyRot = 0f;
                living.yBodyRotO = 0f;
                living.yHeadRot = 0f;
                living.yHeadRotO = 0f;
            }
            entity.setYRot(0f);
            entity.yRotO = 0f;
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(@Nullable Entity entity, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (entity == null) return;
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<Entity, EntityRenderState> renderer =
                (EntityRenderer<Entity, EntityRenderState>) dispatcher.getRenderer(entity);
        if (renderer == null) return;

        try {
            EntityRenderState state = renderer.createRenderState();
            renderer.extractRenderState(entity, state, 0f);

            float bbHeight = Math.max(0.5f, entity.getBbHeight());
            float scale = 0.55f / bbHeight;

            poseStack.pushPose();
            poseStack.translate(0.5f, 0.05f, 0.5f);
            poseStack.scale(scale, scale, scale);

            renderer.submit(state, poseStack, collector, ITEM_CAMERA);

            poseStack.popPose();
        } catch (Exception ignored) {
        }
    }

    private static final CameraRenderState ITEM_CAMERA = new CameraRenderState();

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0f, 0f, 0f));
        output.accept(new Vector3f(1f, 1f, 1f));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Entity> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public WorkSaddleSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new WorkSaddleSpecialRenderer();
        }
    }
}
