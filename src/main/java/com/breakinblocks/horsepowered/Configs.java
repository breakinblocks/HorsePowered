package com.breakinblocks.horsepowered;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configs {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    // Client settings
    public static ForgeConfigSpec.BooleanValue renderItemAmount;
    public static ForgeConfigSpec.BooleanValue mustLookAtBlock;
    public static ForgeConfigSpec.BooleanValue showObstructedPlace;

    // Common settings
    public static ForgeConfigSpec.BooleanValue shouldDamageAxe;
    public static ForgeConfigSpec.BooleanValue choppingBlockDrop;
    public static ForgeConfigSpec.BooleanValue shouldDamageGraniteAnvilPickaxe;
    public static ForgeConfigSpec.BooleanValue graniteAnvilDrop;
    public static ForgeConfigSpec.IntValue pointsForWindup;
    public static ForgeConfigSpec.IntValue pointsPerRotation;
    public static ForgeConfigSpec.IntValue pointsForPress;
    public static ForgeConfigSpec.IntValue choppingMultiplier;
    public static ForgeConfigSpec.IntValue crushingMultiplier;
    public static ForgeConfigSpec.IntValue pressFluidTankSize;
    public static ForgeConfigSpec.DoubleValue grindstoneExhaustion;
    public static ForgeConfigSpec.DoubleValue choppingBlockExhaustion;
    public static ForgeConfigSpec.DoubleValue graniteAnvilExhaustion;

    static {
        // Client config
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
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
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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

            shouldDamageGraniteAnvilPickaxe = builder
                    .comment("If the pickaxe used on the granite anvil should be damaged")
                    .define("shouldDamageGraniteAnvilPickaxe", true);

            graniteAnvilDrop = builder
                    .comment("If true the granite anvil will drop the result items. If false it will put them in internal inventory.")
                    .define("graniteAnvilDrop", true);

            crushingMultiplier = builder
                    .comment("The multiplier for granite anvil crushing time")
                    .defineInRange("crushingMultiplier", 4, 1, Integer.MAX_VALUE);

            graniteAnvilExhaustion = builder
                    .comment("The exhaustion amount added to the player when using the granite anvil (0 to disable)")
                    .defineInRange("graniteAnvilExhaustion", 0.15D, 0.0D, 40.0D);
        }
        builder.pop();
        COMMON_SPEC = builder.build();
    }
}
