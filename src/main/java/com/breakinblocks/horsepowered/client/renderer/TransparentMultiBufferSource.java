package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;

public class TransparentMultiBufferSource implements MultiBufferSource {

    private final MultiBufferSource delegate;
    private final float alpha;

    public TransparentMultiBufferSource(MultiBufferSource delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        VertexConsumer base = delegate.getBuffer(Sheets.translucentItemSheet());
        return new AlphaTintVertexConsumer(base, alpha);
    }
}
