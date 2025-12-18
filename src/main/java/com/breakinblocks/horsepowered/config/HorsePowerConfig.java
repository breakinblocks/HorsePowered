package com.breakinblocks.horsepowered.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HorsePowerConfig {

    public static final ModConfigSpec SPEC;

    // Client settings
    public static ModConfigSpec.BooleanValue renderItemAmount;
    public static ModConfigSpec.BooleanValue mustLookAtBlock;
    public static ModConfigSpec.BooleanValue showObstructedPlace;

    // Common settings
    public static ModConfigSpec.BooleanValue shouldDamageAxe;
    public static ModConfigSpec.BooleanValue choppingBlockDrop;
    public static ModConfigSpec.IntValue pointsForWindup;
    public static ModConfigSpec.IntValue pointsPerRotation;
    public static ModConfigSpec.IntValue pointsForPress;
    public static ModConfigSpec.IntValue choppingMultiplier;
    public static ModConfigSpec.IntValue pressFluidTankSize;
    public static ModConfigSpec.DoubleValue grindstoneExhaustion;
    public static ModConfigSpec.DoubleValue choppingBlockExhaustion;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Client settings").push("client");
        {
            renderItemAmount = builder
                    .comment("If the amount text on how many items is in a stack in a grindstone should render")
                    .define("renderItemAmount", true);

            mustLookAtBlock = builder
                    .comment("Must look at the block to show the amount in it")
                    .define("mustLookAtBlock", true);

            showObstructedPlace = builder
                    .comment("If true will show the area needed when placing a HP block")
                    .define("showObstructedPlace", true);
        }
        builder.pop();

        builder.comment("General settings").push("general");
        {
            shouldDamageAxe = builder
                    .comment("If the item used as an axe for the manual chopping block should be damaged")
                    .define("shouldDamageAxe", true);

            choppingBlockDrop = builder
                    .comment("If true the manual chopping block will drop the result items. If false it will put them in internal inventory.")
                    .define("choppingBlockDrop", true);

            pointsForWindup = builder
                    .comment("The amount of points for the chopper to do windup and do a chop. One lap around the chopping block is 8 points.")
                    .defineInRange("pointsForWindup", 8, 1, Integer.MAX_VALUE);

            pointsPerRotation = builder
                    .comment("The amount of points per rotation with a hand grindstone")
                    .defineInRange("pointsPerRotation", 2, 1, Integer.MAX_VALUE);

            pointsForPress = builder
                    .comment("The amount of points needed for a full press")
                    .defineInRange("pointsForPress", 16, 1, Integer.MAX_VALUE);

            choppingMultiplier = builder
                    .comment("The multiplier for manual chopping time when recipes aren't separated")
                    .defineInRange("choppingMultiplier", 4, 1, Integer.MAX_VALUE);

            pressFluidTankSize = builder
                    .comment("The tank size of the press in mb (1000mb = 1 bucket)")
                    .defineInRange("pressFluidTankSize", 3000, 1000, Integer.MAX_VALUE);

            grindstoneExhaustion = builder
                    .comment("The exhaustion amount added to the player when using the hand grindstone (0 to disable)")
                    .defineInRange("grindstoneExhaustion", 0.1D, 0.0D, 40.0D);

            choppingBlockExhaustion = builder
                    .comment("The exhaustion amount added to the player when using the chopping block (0 to disable)")
                    .defineInRange("choppingBlockExhaustion", 0.1D, 0.0D, 40.0D);
        }
        builder.pop();

        SPEC = builder.build();
    }
}
