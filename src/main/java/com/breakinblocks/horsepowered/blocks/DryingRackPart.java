package com.breakinblocks.horsepowered.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum DryingRackPart implements StringRepresentable {
    MAIN("main", false, false),
    RIGHT("right", true, false),
    FORWARD("forward", false, true),
    RIGHT_FORWARD("right_forward", true, true);

    private final String name;
    private final boolean offsetRight;
    private final boolean offsetForward;

    DryingRackPart(String name, boolean offsetRight, boolean offsetForward) {
        this.name = name;
        this.offsetRight = offsetRight;
        this.offsetForward = offsetForward;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public BlockPos mainFrom(BlockPos thisPos, Direction facing) {
        BlockPos pos = thisPos;
        if (offsetRight) pos = pos.relative(facing.getClockWise().getOpposite());
        if (offsetForward) pos = pos.relative(facing.getOpposite());
        return pos;
    }

    public BlockPos offsetFromMain(BlockPos mainPos, Direction facing) {
        BlockPos pos = mainPos;
        if (offsetRight) pos = pos.relative(facing.getClockWise());
        if (offsetForward) pos = pos.relative(facing);
        return pos;
    }

    public static DryingRackPart[] FILLERS = { RIGHT, FORWARD, RIGHT_FORWARD };
}
