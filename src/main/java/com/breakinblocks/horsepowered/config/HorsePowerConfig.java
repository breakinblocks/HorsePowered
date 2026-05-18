package com.breakinblocks.horsepowered.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HorsePowerConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // Client settings
    public static ModConfigSpec.BooleanValue renderItemAmount;
    public static ModConfigSpec.BooleanValue mustLookAtBlock;
    public static ModConfigSpec.BooleanValue showObstructedPlace;

    // Common settings
    public static ModConfigSpec.BooleanValue shouldDamageAxe;
    public static ModConfigSpec.BooleanValue choppingBlockDrop;
    public static ModConfigSpec.BooleanValue shouldDamageGraniteAnvilPickaxe;
    public static ModConfigSpec.BooleanValue graniteAnvilDrop;
    public static ModConfigSpec.IntValue pointsForWindup;
    public static ModConfigSpec.IntValue pointsPerRotation;
    public static ModConfigSpec.IntValue pointsForPress;
    public static ModConfigSpec.IntValue choppingMultiplier;
    public static ModConfigSpec.IntValue crushingMultiplier;
    public static ModConfigSpec.IntValue pressFluidTankSize;
    public static ModConfigSpec.DoubleValue grindstoneExhaustion;
    public static ModConfigSpec.DoubleValue choppingBlockExhaustion;
    public static ModConfigSpec.DoubleValue graniteAnvilExhaustion;

    static {
        // Client config
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();

        clientBuilder.comment("Client settings").push("client");
        {
            renderItemAmount = clientBuilder
                    .comment("If the amount text on how many items is in a stack in a grindstone should render")
                    .define("renderItemAmount", true);

            mustLookAtBlock = clientBuilder
                    .comment("Must look at the block to show the amount in it")
                    .define("mustLookAtBlock", true);

            showObstructedPlace = clientBuilder
                    .comment("If true will show the area needed when placing a HP block")
                    .define("showObstructedPlace", true);
        }
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();

        // Common config
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();

        commonBuilder.comment("General settings").push("general");
        {
            shouldDamageAxe = commonBuilder
                    .comment("If the item used as an axe for the manual chopping block should be damaged")
                    .define("shouldDamageAxe", true);

            choppingBlockDrop = commonBuilder
                    .comment("If true the manual chopping block will drop the result items. If false it will put them in internal inventory.")
                    .define("choppingBlockDrop", true);

            pointsForWindup = commonBuilder
                    .comment("The amount of points for the chopper to do windup and do a chop. One lap around the chopping block is 8 points.")
                    .defineInRange("pointsForWindup", 8, 1, Integer.MAX_VALUE);

            pointsPerRotation = commonBuilder
                    .comment("The amount of points per rotation with a hand grindstone")
                    .defineInRange("pointsPerRotation", 2, 1, Integer.MAX_VALUE);

            pointsForPress = commonBuilder
                    .comment("The amount of points needed for a full press")
                    .defineInRange("pointsForPress", 16, 1, Integer.MAX_VALUE);

            choppingMultiplier = commonBuilder
                    .comment("The multiplier for manual chopping time when recipes aren't separated")
                    .defineInRange("choppingMultiplier", 4, 1, Integer.MAX_VALUE);

            pressFluidTankSize = commonBuilder
                    .comment("The tank size of the press in mb (1000mb = 1 bucket)")
                    .defineInRange("pressFluidTankSize", 3000, 1000, Integer.MAX_VALUE);

            grindstoneExhaustion = commonBuilder
                    .comment("The exhaustion amount added to the player when using the hand grindstone (0 to disable)")
                    .defineInRange("grindstoneExhaustion", 0.1D, 0.0D, 40.0D);

            choppingBlockExhaustion = commonBuilder
                    .comment("The exhaustion amount added to the player when using the chopping block (0 to disable)")
                    .defineInRange("choppingBlockExhaustion", 0.1D, 0.0D, 40.0D);

            shouldDamageGraniteAnvilPickaxe = commonBuilder
                    .comment("If the pickaxe used on the granite anvil should be damaged")
                    .define("shouldDamageGraniteAnvilPickaxe", true);

            graniteAnvilDrop = commonBuilder
                    .comment("If true the granite anvil will drop the result items. If false it will put them in internal inventory.")
                    .define("graniteAnvilDrop", true);

            crushingMultiplier = commonBuilder
                    .comment("The multiplier for granite anvil crushing time")
                    .defineInRange("crushingMultiplier", 4, 1, Integer.MAX_VALUE);

            graniteAnvilExhaustion = commonBuilder
                    .comment("The exhaustion amount added to the player when using the granite anvil (0 to disable)")
                    .defineInRange("graniteAnvilExhaustion", 0.15D, 0.0D, 40.0D);
        }
        commonBuilder.pop();

        COMMON_SPEC = commonBuilder.build();
    }
}
