package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.items.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;

public class HPRecipeProvider extends RecipeProvider {

    public HPRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildCraftingRecipes(output);
        buildChoppingRecipes(output);
        buildGrindingRecipes(output);
        buildPressingRecipes(output);
        buildDryingRecipes(output);
    }

    // ==================== CRAFTING ====================

    private void buildCraftingRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.HAND_GRINDSTONE.get())
                .pattern("S S")
                .pattern("SSS")
                .pattern("P P")
                .define('S', Items.STONE)
                .define('P', Items.STICK)
                .unlockedBy("has_stone", has(Items.STONE))
                .save(output, HorsePowerMod.id("crafting/hand_grindstone"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.GRINDSTONE.get())
                .pattern("S S")
                .pattern("SSS")
                .pattern("LLL")
                .define('S', Items.STONE)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_stone", has(Items.STONE))
                .save(output, HorsePowerMod.id("crafting/grindstone"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CHOPPING_BLOCK.get())
                .pattern("L")
                .pattern("L")
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_log", has(ItemTags.LOGS))
                .save(output, HorsePowerMod.id("crafting/chopping_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CHOPPER.get())
                .pattern("I I")
                .pattern("LLL")
                .pattern("LLL")
                .define('I', Items.IRON_INGOT)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, HorsePowerMod.id("crafting/chopper"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PRESS.get())
                .pattern("PPP")
                .pattern("P P")
                .pattern("PPP")
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output, HorsePowerMod.id("crafting/press"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.GENERATOR.get())
                .pattern("SRS")
                .pattern("DGD")
                .pattern("DDD")
                .define('S', Items.POLISHED_DEEPSLATE_SLAB)
                .define('R', Items.LIGHTNING_ROD)
                .define('D', Items.POLISHED_DEEPSLATE)
                .define('G', ModBlocks.GRINDSTONE.get())
                .unlockedBy("has_grindstone", has(ModBlocks.GRINDSTONE.get()))
                .save(output, HorsePowerMod.id("crafting/generator"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.DOUGH.get())
                .requires(ModItems.FLOUR.get())
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_flour", has(ModItems.FLOUR.get()))
                .save(output, HorsePowerMod.id("crafting/dough"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.WORK_SADDLE.get())
                .requires(Items.SADDLE)
                .requires(Items.LEAD)
                .unlockedBy("has_saddle", has(Items.SADDLE))
                .save(output, HorsePowerMod.id("crafting/work_saddle"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DRYING_RACK.get())
                .pattern("SSS")
                .pattern("P P")
                .pattern("SSS")
                .define('S', Items.STICK)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output, HorsePowerMod.id("crafting/drying_rack"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.WOODEN_HOPPER.get())
                .pattern("S S")
                .pattern("SCS")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('C', Items.CHEST)
                .unlockedBy("has_chest", has(Items.CHEST))
                .save(output, HorsePowerMod.id("crafting/wooden_hopper"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.DOUGH.get()), RecipeCategory.FOOD,
                        Items.BREAD, 0.35f, 200)
                .unlockedBy("has_dough", has(ModItems.DOUGH.get()))
                .save(output, HorsePowerMod.id("smelting/dough_to_bread"));
    }

    // ==================== CHOPPING ====================

    private void buildChoppingRecipes(RecipeOutput output) {
        // Logs to planks (all 1.21.1 wood types — no pale_oak)
        chop(output, Ingredient.of(ItemTags.OAK_LOGS), Items.OAK_PLANKS, 4, "oak_log_to_planks");
        chop(output, Ingredient.of(ItemTags.BIRCH_LOGS), Items.BIRCH_PLANKS, 4, "birch_log_to_planks");
        chop(output, Ingredient.of(ItemTags.SPRUCE_LOGS), Items.SPRUCE_PLANKS, 4, "spruce_log_to_planks");
        chop(output, Ingredient.of(ItemTags.JUNGLE_LOGS), Items.JUNGLE_PLANKS, 4, "jungle_log_to_planks");
        chop(output, Ingredient.of(ItemTags.ACACIA_LOGS), Items.ACACIA_PLANKS, 4, "acacia_log_to_planks");
        chop(output, Ingredient.of(ItemTags.DARK_OAK_LOGS), Items.DARK_OAK_PLANKS, 4, "dark_oak_log_to_planks");
        chop(output, Ingredient.of(ItemTags.MANGROVE_LOGS), Items.MANGROVE_PLANKS, 4, "mangrove_log_to_planks");
        chop(output, Ingredient.of(ItemTags.CHERRY_LOGS), Items.CHERRY_PLANKS, 4, "cherry_log_to_planks");
        chop(output, Ingredient.of(ItemTags.CRIMSON_STEMS), Items.CRIMSON_PLANKS, 4, "crimson_stem_to_planks");
        chop(output, Ingredient.of(ItemTags.WARPED_STEMS), Items.WARPED_PLANKS, 4, "warped_stem_to_planks");
        chop(output, Ingredient.of(ItemTags.BAMBOO_BLOCKS), Items.BAMBOO_PLANKS, 4, "bamboo_block_to_planks");

        // Planks to sticks
        chop(output, Ingredient.of(ItemTags.PLANKS), Items.STICK, 6, "planks_to_sticks");

        // Food splitting
        chop(output, Ingredient.of(Items.MELON), Items.MELON_SLICE, 9, "melon_to_slices");
        chop(output, Ingredient.of(Items.PUMPKIN), Items.PUMPKIN_SEEDS, 4, "pumpkin_to_seeds");

        // Wood item recycling
        chop(output, Ingredient.of(ItemTags.WOODEN_DOORS), Items.STICK, 4, "wooden_door_to_sticks");
        chop(output, Ingredient.of(ItemTags.WOODEN_TRAPDOORS), Items.STICK, 6, "wooden_trapdoor_to_sticks");
        chop(output, Ingredient.of(ItemTags.WOODEN_PRESSURE_PLATES), Items.STICK, 2, "wooden_pressure_plate_to_sticks");
        chop(output, Ingredient.of(ItemTags.WOODEN_FENCES), Items.STICK, 3, "wooden_fence_to_sticks");
        chop(output, Ingredient.of(ItemTags.FENCE_GATES), Items.STICK, 4, "fence_gate_to_sticks");
        chop(output, Ingredient.of(ItemTags.WOODEN_SLABS), Items.STICK, 3, "wooden_slab_to_sticks");
        chop(output, Ingredient.of(ItemTags.WOODEN_STAIRS), Items.STICK, 6, "wooden_stairs_to_sticks");
        chop(output, Ingredient.of(ItemTags.WOODEN_BUTTONS), Items.STICK, 1, "wooden_button_to_stick");
        chop(output, Ingredient.of(ItemTags.SIGNS), Items.STICK, 3, "sign_to_sticks");
        chop(output, Ingredient.of(Items.LADDER), Items.STICK, 3, "ladder_to_sticks");

        // Bamboo
        chop(output, Ingredient.of(Items.BAMBOO), Items.STICK, 2, "bamboo_to_sticks");
    }

    // ==================== GRINDING ====================

    private void buildGrindingRecipes(RecipeOutput output) {
        // Bone processing
        GrindingRecipeBuilder.grinding(Ingredient.of(Items.BONE), Items.BONE_MEAL, 3)
                .secondary(Items.BONE_MEAL, 1, 25)
                .time(12)
                .save(output, "bone_to_bonemeal");
        grind(output, Ingredient.of(Items.BONE_BLOCK), Items.BONE_MEAL, 9, 12, "bone_block_to_bonemeal");

        // Flour
        GrindingRecipeBuilder.grinding(Ingredient.of(Items.WHEAT), ModItems.FLOUR.get())
                .time(12)
                .save(output, "wheat_to_flour");

        // Blaze / Breeze
        grind(output, Ingredient.of(Items.BLAZE_ROD), Items.BLAZE_POWDER, 4, 8, "blaze_rod_to_powder");
        grind(output, Ingredient.of(Items.BREEZE_ROD), Items.WIND_CHARGE, 5, 8, "breeze_rod_to_wind_charge");

        // Stone chain
        GrindingRecipeBuilder.grinding(Ingredient.of(Items.COBBLESTONE), Items.GRAVEL, 1)
                .secondary(Items.FLINT, 1, 10)
                .time(8)
                .save(output, "cobblestone_to_gravel");
        GrindingRecipeBuilder.grinding(Ingredient.of(Items.GRAVEL), Items.SAND, 1)
                .secondary(Items.FLINT, 1, 15)
                .time(8)
                .save(output, "gravel_to_sand");
        grind(output, Ingredient.of(Items.STONE), Items.COBBLESTONE, 1, 8, "stone_to_cobblestone");
        grind(output, Ingredient.of(Items.COBBLED_DEEPSLATE), Items.GRAVEL, 1, 8, "cobbled_deepslate_to_gravel");
        grind(output, Ingredient.of(Items.NETHERRACK), Items.NETHER_BRICK, 1, 8, "netherrack_to_nether_brick");

        // Sand from sandstone
        grind(output, Ingredient.of(Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE), Items.SAND, 2, 8, "sandstone_to_sand");
        grind(output, Ingredient.of(Items.RED_SANDSTONE, Items.CHISELED_RED_SANDSTONE, Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE), Items.RED_SAND, 2, 8, "red_sandstone_to_sand");

        // Wool to string
        grind(output, Ingredient.of(ItemTags.WOOL), Items.STRING, 4, 8, "wool_to_string");

        // Block decomposition
        grind(output, Ingredient.of(Items.BRICKS), Items.BRICK, 4, 8, "bricks_to_brick");
        grind(output, Ingredient.of(Items.NETHER_BRICKS), Items.NETHER_BRICK, 4, 8, "nether_bricks_to_nether_brick");
        grind(output, Ingredient.of(Items.CLAY), Items.CLAY_BALL, 4, 8, "clay_to_clay_ball");
        grind(output, Ingredient.of(Items.GLOWSTONE), Items.GLOWSTONE_DUST, 4, 8, "glowstone_to_dust");
        grind(output, Ingredient.of(Items.HONEYCOMB_BLOCK), Items.HONEYCOMB, 4, 8, "honeycomb_block_to_honeycomb");
        grind(output, Ingredient.of(Items.DRIPSTONE_BLOCK), Items.POINTED_DRIPSTONE, 4, 8, "dripstone_block_to_pointed");
        grind(output, Ingredient.of(Items.PRISMARINE), Items.PRISMARINE_SHARD, 4, 12, "prismarine_to_shard");
        grind(output, Ingredient.of(Items.PRISMARINE_BRICKS), Items.PRISMARINE_SHARD, 9, 12, "prismarine_bricks_to_shard");
        grind(output, Ingredient.of(Items.QUARTZ_BLOCK), Items.QUARTZ, 4, 8, "quartz_block_to_quartz");

        // Miscellaneous
        grind(output, Ingredient.of(Items.FLINT), Items.GUNPOWDER, 1, 12, "flint_to_gunpowder");
        grind(output, Ingredient.of(Items.SOUL_SOIL), Items.SOUL_SAND, 1, 8, "soul_soil_to_soul_sand");
        grind(output, Ingredient.of(Items.SUGAR_CANE), Items.SUGAR, 2, 8, "sugar_cane_to_sugar");

        // Ore grinding
        GrindingRecipeBuilder.grinding(Ingredient.of(Items.RAW_IRON), Items.IRON_NUGGET, 12)
                .secondary(Items.IRON_NUGGET, 3, 25)
                .time(16)
                .save(output, "raw_iron_to_nuggets");
        GrindingRecipeBuilder.grinding(Ingredient.of(Items.RAW_GOLD), Items.GOLD_NUGGET, 12)
                .secondary(Items.GOLD_NUGGET, 3, 25)
                .time(16)
                .save(output, "raw_gold_to_nuggets");
        GrindingRecipeBuilder.grinding(Ingredient.of(Items.RAW_COPPER), Items.COPPER_INGOT, 1)
                .secondary(Items.COPPER_INGOT, 1, 25)
                .time(16)
                .save(output, "raw_copper_to_ingots");
    }

    // ==================== PRESSING ====================

    private void buildPressingRecipes(RecipeOutput output) {
        // Seeds to seed oil
        PressingRecipeBuilder.pressing(Ingredient.of(Items.WHEAT_SEEDS))
                .inputCount(12)
                .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                .save(output, "seeds_to_oil");
        PressingRecipeBuilder.pressing(Ingredient.of(Items.MELON_SEEDS))
                .inputCount(12)
                .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                .save(output, "melon_seeds_to_oil");
        PressingRecipeBuilder.pressing(Ingredient.of(Items.PUMPKIN_SEEDS))
                .inputCount(12)
                .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                .save(output, "pumpkin_seeds_to_oil");
        PressingRecipeBuilder.pressing(Ingredient.of(Items.BEETROOT_SEEDS))
                .inputCount(12)
                .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                .save(output, "beetroot_seeds_to_oil");
        PressingRecipeBuilder.pressing(Ingredient.of(Items.TORCHFLOWER_SEEDS))
                .inputCount(12)
                .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                .save(output, "torchflower_seeds_to_oil");

        // Sugar cane to paper
        PressingRecipeBuilder.pressing(Ingredient.of(Items.SUGAR_CANE))
                .inputCount(3)
                .result(Items.PAPER, 3)
                .save(output, "sugar_cane_to_paper");

        // Flowers to dye (small flowers = 4, tall = 8)
        press(output, Items.DANDELION, Items.YELLOW_DYE, 4, "dandelion_to_dye");
        press(output, Items.POPPY, Items.RED_DYE, 4, "poppy_to_dye");
        press(output, Items.BLUE_ORCHID, Items.LIGHT_BLUE_DYE, 4, "blue_orchid_to_dye");
        press(output, Items.ALLIUM, Items.MAGENTA_DYE, 4, "allium_to_dye");
        press(output, Items.AZURE_BLUET, Items.LIGHT_GRAY_DYE, 4, "azure_bluet_to_dye");
        press(output, Items.RED_TULIP, Items.RED_DYE, 4, "red_tulip_to_dye");
        press(output, Items.ORANGE_TULIP, Items.ORANGE_DYE, 4, "orange_tulip_to_dye");
        press(output, Items.WHITE_TULIP, Items.WHITE_DYE, 4, "white_tulip_to_dye");
        press(output, Items.PINK_TULIP, Items.PINK_DYE, 4, "pink_tulip_to_dye");
        press(output, Items.OXEYE_DAISY, Items.LIGHT_GRAY_DYE, 4, "oxeye_daisy_to_dye");
        press(output, Items.CORNFLOWER, Items.BLUE_DYE, 4, "cornflower_to_dye");
        press(output, Items.LILY_OF_THE_VALLEY, Items.WHITE_DYE, 4, "lily_of_the_valley_to_dye");
        press(output, Items.WITHER_ROSE, Items.BLACK_DYE, 4, "wither_rose_to_dye");
        press(output, Items.TORCHFLOWER, Items.ORANGE_DYE, 4, "torchflower_to_dye");
        press(output, Items.SUNFLOWER, Items.YELLOW_DYE, 8, "sunflower_to_dye");
        press(output, Items.LILAC, Items.MAGENTA_DYE, 8, "lilac_to_dye");
        press(output, Items.ROSE_BUSH, Items.RED_DYE, 8, "rose_bush_to_dye");
        press(output, Items.PEONY, Items.PINK_DYE, 8, "peony_to_dye");
        press(output, Items.PITCHER_PLANT, Items.CYAN_DYE, 8, "pitcher_plant_to_dye");

        // Honey
        press(output, Items.HONEY_BOTTLE, Items.SUGAR, 4, "honey_to_sugar");
        press(output, Items.HONEYCOMB, Items.HONEY_BOTTLE, 1, "honeycomb_to_honey");

        // Cactus
        press(output, Items.CACTUS, Items.GREEN_DYE, 4, "cactus_to_dye");

        // Water extraction
        press(output, Items.WET_SPONGE, Items.SPONGE, 1, "wet_sponge_to_sponge");
        press(output, Items.MUD, Items.DIRT, 1, "mud_to_dirt");

        PressingRecipeBuilder.pressing(Ingredient.of(Items.ICE))
                .inputCount(1)
                .fluidResult(Fluids.WATER, 1000)
                .save(output, "ice_to_water");
        PressingRecipeBuilder.pressing(Ingredient.of(Items.SNOW_BLOCK))
                .inputCount(1)
                .fluidResult(Fluids.WATER, 1000)
                .save(output, "snow_to_water");
        PressingRecipeBuilder.pressing(Ingredient.of(Items.SNOWBALL))
                .inputCount(4)
                .fluidResult(Fluids.WATER, 1000)
                .save(output, "snowball_to_water");
        PressingRecipeBuilder.pressing(Ingredient.of(Items.PACKED_ICE))
                .inputCount(1)
                .fluidResult(Fluids.WATER, 4000)
                .save(output, "packed_ice_to_water");

        // Magma cream
        press(output, Items.MAGMA_BLOCK, Items.MAGMA_CREAM, 4, "magma_block_to_cream");

        // Dripstone → water
        PressingRecipeBuilder.pressing(Ingredient.of(Items.POINTED_DRIPSTONE))
                .inputCount(4)
                .fluidResult(Fluids.WATER, 500)
                .save(output, "pointed_dripstone_to_water");

        // Kelp drying
        PressingRecipeBuilder.pressing(Ingredient.of(Items.KELP))
                .inputCount(8)
                .result(Items.DRIED_KELP, 4)
                .save(output, "kelp_to_dried_kelp");
    }

    // ==================== DRYING ====================

    private void buildDryingRecipes(RecipeOutput output) {
        DryingRecipeBuilder.drying(Ingredient.of(Items.KELP), Items.DRIED_KELP)
                .time(1000)
                .save(output, "kelp_to_dried_kelp");
        DryingRecipeBuilder.drying(Ingredient.of(Items.WET_SPONGE), Items.SPONGE)
                .time(2000)
                .save(output, "wet_sponge_to_sponge");
        DryingRecipeBuilder.drying(Ingredient.of(Items.ROTTEN_FLESH), Items.LEATHER)
                .time(1000)
                .save(output, "rotten_flesh_to_leather");
        DryingRecipeBuilder.drying(Ingredient.of(Items.MUD), Items.DIRT)
                .time(1000)
                .save(output, "mud_to_dirt");
        DryingRecipeBuilder.drying(Ingredient.of(Items.CLAY), Items.TERRACOTTA)
                .time(4000)
                .save(output, "clay_to_terracotta");
        DryingRecipeBuilder.drying(Ingredient.of(ItemTags.SAPLINGS), Items.DEAD_BUSH)
                .time(1500)
                .save(output, "saplings_to_dead_bush");
    }

    // ==================== HELPERS ====================

    private void chop(RecipeOutput output, Ingredient input, Item result, int count, String name) {
        ChoppingRecipeBuilder.chopping(input, result, count).time(1).save(output, name);
    }

    private void grind(RecipeOutput output, Ingredient input, Item result, int count, int time, String name) {
        GrindingRecipeBuilder.grinding(input, result, count).time(time).save(output, name);
    }

    private void press(RecipeOutput output, Item input, Item result, int count, String name) {
        PressingRecipeBuilder.pressing(Ingredient.of(input)).inputCount(1).result(result, count).save(output, name);
    }
}
