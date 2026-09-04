package com.breakinblocks.horsepowered;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static ForgeConfigSpec.IntValue pathObstructionTolerance;
    public static ForgeConfigSpec.DoubleValue pathSpeedDefault;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> pathSpeedEntries;

    public static ForgeConfigSpec.IntValue horseEngineRpm;
    public static ForgeConfigSpec.DoubleValue horseEngineStressPerJumpPoint;
    public static ForgeConfigSpec.DoubleValue horseEngineDefaultJumpStrength;

    private static volatile Map<ResourceLocation, Double> cachedPathSpeedMap;

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

        builder.comment("Horse path settings").push("horse_path");
        {
            pathObstructionTolerance = builder
                    .comment("How many non-replaceable blocks (chests, hoppers, gears, etc.) are tolerated in the horse's 7x7 working ring before validation fails. Levers are always allowed.")
                    .defineInRange("pathObstructionTolerance", 2, 0, 40);

            pathSpeedDefault = builder
                    .comment("Speed multiplier used for any path floor block not listed in pathSpeedEntries.")
                    .defineInRange("pathSpeedDefault", 1.0D, 0.0D, 10.0D);

            pathSpeedEntries = builder
                    .comment(
                            "Per-block speed multipliers for the floor blocks under the horse's circular path.",
                            "Format: \"namespace:block_id=multiplier\". Multipliers below 1.0 slow the worker, above 1.0 speed it up.",
                            "The final path speed is the average of the multipliers of every unique floor block the path crosses.")
                    .defineListAllowEmpty("pathSpeedEntries",
                            List.of(
                                    "minecraft:grass_block=0.5",
                                    "minecraft:dirt=0.5",
                                    "minecraft:coarse_dirt=0.5",
                                    "minecraft:rooted_dirt=0.5",
                                    "minecraft:dirt_path=1.0",
                                    "minecraft:packed_ice=2.0"),
                            Configs::isValidSpeedEntry);
        }
        builder.pop();

        builder.comment("Horse engine settings (only used when Create is installed)").push("horse_engine");
        {
            horseEngineRpm = builder
                    .comment("Rotation speed in RPM produced by a horse engine while its worker walks.")
                    .defineInRange("horseEngineRpm", 16, 1, 256);

            horseEngineStressPerJumpPoint = builder
                    .comment("Stress units provided at the configured RPM for every 0.1 of the worker's jump strength.")
                    .defineInRange("horseEngineStressPerJumpPoint", 40.0D, 0.0D, 4096.0D);

            horseEngineDefaultJumpStrength = builder
                    .comment("Jump strength used for workers that have no jump strength attribute.",
                            "Per-mob values can be set with datapack files in data/<namespace>/horse_engine_jump_strength/.")
                    .defineInRange("horseEngineDefaultJumpStrength", 0.4D, 0.0D, 32.0D);
        }
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private static boolean isValidSpeedEntry(Object obj) {
        if (!(obj instanceof String s)) return false;
        int eq = s.indexOf('=');
        if (eq <= 0 || eq == s.length() - 1) return false;
        if (ResourceLocation.tryParse(s.substring(0, eq).trim()) == null) return false;
        try {
            Double.parseDouble(s.substring(eq + 1).trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static double getPathSpeedMultiplier(ResourceLocation blockId) {
        Map<ResourceLocation, Double> map = cachedPathSpeedMap;
        if (map == null) {
            map = parseSpeedMap();
            cachedPathSpeedMap = map;
        }
        Double value = map.get(blockId);
        return value != null ? value : pathSpeedDefault.get();
    }

    public static void invalidatePathSpeedCache() {
        cachedPathSpeedMap = null;
    }

    private static Map<ResourceLocation, Double> parseSpeedMap() {
        Map<ResourceLocation, Double> map = new HashMap<>();
        for (String entry : pathSpeedEntries.get()) {
            int eq = entry.indexOf('=');
            if (eq <= 0) continue;
            ResourceLocation id = ResourceLocation.tryParse(entry.substring(0, eq).trim());
            if (id == null) continue;
            try {
                map.put(id, Double.parseDouble(entry.substring(eq + 1).trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }
}
