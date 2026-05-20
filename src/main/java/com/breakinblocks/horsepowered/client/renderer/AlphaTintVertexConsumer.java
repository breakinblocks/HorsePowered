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
    public VertexConsumer vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        int newA = Math.round(a * alphaMultiplier);
        if (newA < 0) newA = 0;
        if (newA > 255) newA = 255;
        delegate.color(r, g, b, newA);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        delegate.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        delegate.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        delegate.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}
