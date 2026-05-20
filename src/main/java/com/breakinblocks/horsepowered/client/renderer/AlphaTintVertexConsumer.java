package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class AlphaTintVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final float alphaMultiplier;

    public AlphaTintVertexConsumer(VertexConsumer delegate, float alphaMultiplier) {
        this.delegate = delegate;
        this.alphaMultiplier = Math.max(0F, Math.min(1F, alphaMultiplier));
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        int newA = Math.round(a * alphaMultiplier);
        if (newA < 0) newA = 0;
        if (newA > 255) newA = 255;
        delegate.setColor(r, g, b, newA);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }
}
