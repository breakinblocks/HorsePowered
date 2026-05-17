package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WorkSaddleItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final String NBT_KEY = "CarriedEntity";
    private static final Map<UUID, Entity> CACHE = new HashMap<>();

    public WorkSaddleItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        ItemStack saddleStack = new ItemStack(Items.SADDLE);
        BakedModel saddleModel = itemRenderer.getModel(saddleStack, mc.level, null, 0);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buf, Sheets.translucentItemSheet(), true, false);
        itemRenderer.renderModelLists(saddleModel, saddleStack, light, overlay, pose, consumer);

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_KEY)) return;
        CompoundTag entityTag = tag.getCompound(NBT_KEY);
        if (entityTag.isEmpty() || mc.level == null) return;

        Entity entity = resolveEntity(entityTag, mc);
        if (entity == null) return;

        pose.pushPose();
        pose.translate(0.5D, 0.85D, 0.5D);
        float scale = 0.35F;
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.YP.rotationDegrees(180F));
        pose.mulPose(Axis.XP.rotationDegrees(-12F));

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(entity, 0.0D, -entity.getBbHeight() / 2.0D, 0.0D, 0F, 0F, pose, buf, light);
        dispatcher.setRenderShadow(true);

        pose.popPose();
    }

    private static Entity resolveEntity(CompoundTag entityTag, Minecraft mc) {
        UUID uuid = entityTag.hasUUID("UUID") ? entityTag.getUUID("UUID") : null;
        if (uuid != null) {
            Entity cached = CACHE.get(uuid);
            if (cached != null && cached.getType().getDescriptionId().equals(typeOf(entityTag))) {
                return cached;
            }
        }
        Optional<Entity> created = EntityType.create(entityTag, mc.level);
        if (created.isEmpty()) return null;
        Entity entity = created.get();
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0F;
            living.yHeadRot = 0F;
            living.setYRot(0F);
            living.setXRot(0F);
            living.yBodyRotO = 0F;
            living.yHeadRotO = 0F;
            living.yRotO = 0F;
            living.xRotO = 0F;
        }
        if (uuid != null) CACHE.put(uuid, entity);
        return entity;
    }

    private static String typeOf(CompoundTag entityTag) {
        String id = entityTag.getString("id");
        EntityType<?> type = EntityType.byString(id).orElse(null);
        return type == null ? "" : type.getDescriptionId();
    }
}
