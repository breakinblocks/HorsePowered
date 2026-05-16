package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class WorkSaddleItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ModelResourceLocation SADDLE_MODEL =
            ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("saddle"));

    private static final int CACHE_LIMIT = 32;
    private final Map<CompoundTag, Entity> entityCache =
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<CompoundTag, Entity> eldest) {
                    return size() > CACHE_LIMIT;
                }
            };

    public WorkSaddleItemRenderer(BlockEntityRenderDispatcher dispatcher, net.minecraft.client.model.geom.EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack ps, MultiBufferSource buf, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel saddleModel = mc.getModelManager().getModel(SADDLE_MODEL);
        ItemStack saddleProxy = new ItemStack(Items.SADDLE);
        VertexConsumer cons = ItemRenderer.getFoilBufferDirect(buf, Sheets.translucentItemSheet(), true, false);
        mc.getItemRenderer().renderModelLists(saddleModel, saddleProxy, light, overlay, ps, cons);

        CustomData stored = stack.get(DataComponents.ENTITY_DATA);
        if (stored == null || stored.isEmpty()) return;

        Entity entity = getCachedEntity(stored);
        if (entity == null) return;

        renderEntityOverlay(entity, ps, buf, light);
    }

    private void renderEntityOverlay(Entity entity, PoseStack ps, MultiBufferSource buf, int light) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        float maxDim = Math.max(0.5f, Math.max(entity.getBbWidth(), entity.getBbHeight()));
        float scale = 0.5f / maxDim;

        ps.pushPose();
        ps.translate(0.5f, 0.15f, 0.5f);
        ps.scale(scale, scale, scale);
        dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 0.0f, ps, buf, light);
        ps.popPose();
    }

    private Entity getCachedEntity(CustomData stored) {
        CompoundTag tag = stored.copyTag();
        Entity cached = entityCache.get(tag);
        if (cached != null) return cached;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        Optional<Entity> created = EntityType.create(tag, level);
        if (created.isEmpty()) return null;

        Entity entity = created.get();
        entity.setNoGravity(true);
        entity.setSilent(true);
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0f;
            living.yBodyRotO = 0f;
            living.yHeadRot = 0f;
            living.yHeadRotO = 0f;
        }
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        entity.setYRot(0f);
        entity.yRotO = 0f;

        entityCache.put(tag, entity);
        return entity;
    }
}
