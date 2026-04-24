package com.breakinblocks.horsepowered.recipes;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum RecipeTier implements StringRepresentable {
    ANY("any"),
    HAND_ONLY("hand_only"),
    HORSE_ONLY("horse_only");

    public static final Codec<RecipeTier> CODEC = StringRepresentable.fromEnum(RecipeTier::values);
    public static final StreamCodec<ByteBuf, RecipeTier> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], RecipeTier::ordinal);

    private final String name;

    RecipeTier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean allowsHand() {
        return this != HORSE_ONLY;
    }

    public boolean allowsHorse() {
        return this != HAND_ONLY;
    }
}
