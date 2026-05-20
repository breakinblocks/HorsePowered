package com.breakinblocks.horsepowered.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;

/**
 * Wraps a {@link MultiBufferSource} and forces every requested render type to
 * the translucent item sheet, then tints vertex alpha by the supplied factor.
 * Used to fade items in/out at the same world position.
 *
 * Item textures are typically rendered with cutout, which short-circuits on a
 * binary alpha threshold and ignores vertex alpha. Routing through the
 * translucent item render type makes the vertex alpha actually blend.
 */
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
