package com.breakinblocks.horsepowered.config;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static ModConfigSpec.IntValue pathObstructionTolerance;
    public static ModConfigSpec.DoubleValue pathSpeedDefault;
    public static ModConfigSpec.ConfigValue<List<? extends String>> pathSpeedEntries;

    private static volatile Map<Identifier, Double> cachedPathSpeedMap;

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

        commonBuilder.comment("Horse path settings").push("horse_path");
        {
            pathObstructionTolerance = commonBuilder
                    .comment("How many non-replaceable blocks (chests, hoppers, gears, etc.) are tolerated in the horse's 7x7 working ring before validation fails. Levers are always allowed.")
                    .defineInRange("pathObstructionTolerance", 2, 0, 40);

            pathSpeedDefault = commonBuilder
                    .comment("Speed multiplier used for any path floor block not listed in pathSpeedEntries.")
                    .defineInRange("pathSpeedDefault", 1.0D, 0.0D, 10.0D);

            pathSpeedEntries = commonBuilder
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
                            () -> "minecraft:stone=1.0",
                            HorsePowerConfig::isValidSpeedEntry);
        }
        commonBuilder.pop();

        COMMON_SPEC = commonBuilder.build();
    }

    private static boolean isValidSpeedEntry(Object obj) {
        if (!(obj instanceof String s)) return false;
        int eq = s.indexOf('=');
        if (eq <= 0 || eq == s.length() - 1) return false;
        if (Identifier.tryParse(s.substring(0, eq).trim()) == null) return false;
        try {
            Double.parseDouble(s.substring(eq + 1).trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static double getPathSpeedMultiplier(Identifier blockId) {
        Map<Identifier, Double> map = cachedPathSpeedMap;
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

    private static Map<Identifier, Double> parseSpeedMap() {
        Map<Identifier, Double> map = new HashMap<>();
        for (String entry : pathSpeedEntries.get()) {
            int eq = entry.indexOf('=');
            if (eq <= 0) continue;
            Identifier id = Identifier.tryParse(entry.substring(0, eq).trim());
            if (id == null) continue;
            try {
                map.put(id, Double.parseDouble(entry.substring(eq + 1).trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }
}
