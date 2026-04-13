package com.breakinblocks.horsepowered.datagen;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.items.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;

public class HPRecipeProvider extends RecipeProvider.Runner {

    public HPRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new HPRecipes(registries, output);
    }

    @Override
    public String getName() {
        return "Horse Powered Recipes";
    }

    private static class HPRecipes extends RecipeProvider {

        protected HPRecipes(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        protected void buildRecipes() {
            buildCraftingRecipes();
            buildChoppingRecipes();
            buildGrindingRecipes();
            buildPressingRecipes();
        }

        private void buildCraftingRecipes() {
            // Guide book recipe is hand-authored in guide.json because it outputs
            // guideme:guide with a custom component that ShapedRecipeBuilder can't express.

            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.HAND_GRINDSTONE.get())
                    .pattern("S S")
                    .pattern("SSS")
                    .pattern("P P")
                    .define('S', Items.STONE)
                    .define('P', Items.STICK)
                    .unlockedBy("has_stone", has(Items.STONE))
                    .save(this.output, recipeKey("crafting/hand_grindstone"));

            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.GRINDSTONE.get())
                    .pattern("S S")
                    .pattern("SSS")
                    .pattern("LLL")
                    .define('S', Items.STONE)
                    .define('L', ItemTags.LOGS)
                    .unlockedBy("has_stone", has(Items.STONE))
                    .save(this.output, recipeKey("crafting/grindstone"));

            ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.CHOPPING_BLOCK.get())
                    .requires(tag(ItemTags.LOGS))
                    .requires(tag(ItemTags.WOODEN_SLABS))
                    .unlockedBy("has_log", has(ItemTags.LOGS))
                    .save(this.output, recipeKey("crafting/chopping_block"));

            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.CHOPPER.get())
                    .pattern("I I")
                    .pattern("LLL")
                    .pattern("LLL")
                    .define('I', Items.IRON_INGOT)
                    .define('L', ItemTags.LOGS)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(this.output, recipeKey("crafting/chopper"));

            ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.DECORATIONS, ModBlocks.PRESS.get())
                    .pattern("PPP")
                    .pattern("P P")
                    .pattern("PPP")
                    .define('P', ItemTags.PLANKS)
                    .unlockedBy("has_planks", has(ItemTags.PLANKS))
                    .save(this.output, recipeKey("crafting/press"));

            ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.FOOD, ModItems.DOUGH.get())
                    .requires(ModItems.FLOUR.get())
                    .requires(Items.WATER_BUCKET)
                    .unlockedBy("has_flour", has(ModItems.FLOUR.get()))
                    .save(this.output, recipeKey("crafting/dough"));

            ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.TOOLS, ModItems.WORK_SADDLE.get())
                    .requires(Items.SADDLE)
                    .requires(Items.LEAD)
                    .unlockedBy("has_saddle", has(Items.SADDLE))
                    .save(this.output, recipeKey("crafting/work_saddle"));

            SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.DOUGH.get()), RecipeCategory.FOOD, net.minecraft.world.item.crafting.CookingBookCategory.MISC, Items.BREAD, 0.35f, 200)
                    .unlockedBy("has_dough", has(ModItems.DOUGH.get()))
                    .save(this.output, recipeKey("smelting/dough_to_bread"));
        }

        private void buildChoppingRecipes() {
            chop(tag(ItemTags.OAK_LOGS), Items.OAK_PLANKS, 4, 1, "oak_log_to_planks");
            chop(tag(ItemTags.BIRCH_LOGS), Items.BIRCH_PLANKS, 4, 1, "birch_log_to_planks");
            chop(tag(ItemTags.SPRUCE_LOGS), Items.SPRUCE_PLANKS, 4, 1, "spruce_log_to_planks");
            chop(tag(ItemTags.JUNGLE_LOGS), Items.JUNGLE_PLANKS, 4, 1, "jungle_log_to_planks");
            chop(tag(ItemTags.ACACIA_LOGS), Items.ACACIA_PLANKS, 4, 1, "acacia_log_to_planks");
            chop(tag(ItemTags.DARK_OAK_LOGS), Items.DARK_OAK_PLANKS, 4, 1, "dark_oak_log_to_planks");
            chop(tag(ItemTags.MANGROVE_LOGS), Items.MANGROVE_PLANKS, 4, 1, "mangrove_log_to_planks");
            chop(tag(ItemTags.CHERRY_LOGS), Items.CHERRY_PLANKS, 4, 1, "cherry_log_to_planks");
            chop(tag(ItemTags.PALE_OAK_LOGS), Items.PALE_OAK_PLANKS, 4, 1, "pale_oak_log_to_planks");
            chop(tag(ItemTags.CRIMSON_STEMS), Items.CRIMSON_PLANKS, 4, 1, "crimson_stem_to_planks");
            chop(tag(ItemTags.WARPED_STEMS), Items.WARPED_PLANKS, 4, 1, "warped_stem_to_planks");
            chop(tag(ItemTags.BAMBOO_BLOCKS), Items.BAMBOO_PLANKS, 4, 1, "bamboo_block_to_planks");

            chop(tag(ItemTags.PLANKS), Items.STICK, 6, 1, "planks_to_sticks");

            chop(Ingredient.of(Items.MELON), Items.MELON_SLICE, 9, 1, "melon_to_slices");
            chop(Ingredient.of(Items.PUMPKIN), Items.PUMPKIN_SEEDS, 4, 1, "pumpkin_to_seeds");

            chop(tag(ItemTags.WOODEN_DOORS), Items.STICK, 4, 1, "wooden_door_to_sticks");
            chop(tag(ItemTags.WOODEN_TRAPDOORS), Items.STICK, 6, 1, "wooden_trapdoor_to_sticks");
            chop(tag(ItemTags.WOODEN_PRESSURE_PLATES), Items.STICK, 2, 1, "wooden_pressure_plate_to_sticks");
            chop(tag(ItemTags.WOODEN_FENCES), Items.STICK, 3, 1, "wooden_fence_to_sticks");
            chop(tag(ItemTags.FENCE_GATES), Items.STICK, 4, 1, "fence_gate_to_sticks");
            chop(tag(ItemTags.WOODEN_SLABS), Items.STICK, 3, 1, "wooden_slab_to_sticks");
            chop(tag(ItemTags.WOODEN_STAIRS), Items.STICK, 6, 1, "wooden_stairs_to_sticks");
            chop(tag(ItemTags.WOODEN_BUTTONS), Items.STICK, 1, 1, "wooden_button_to_stick");
            chop(tag(ItemTags.SIGNS), Items.STICK, 3, 1, "sign_to_sticks");
            chop(Ingredient.of(Items.LADDER), Items.STICK, 3, 1, "ladder_to_sticks");

            chop(Ingredient.of(Items.BAMBOO), Items.STICK, 2, 1, "bamboo_to_sticks");
        }

        private void buildGrindingRecipes() {
            GrindingRecipeBuilder.grinding(Ingredient.of(Items.BONE), Items.BONE_MEAL, 3)
                    .secondary(Items.BONE_MEAL, 1, 25)
                    .time(12)
                    .save(this.output, "bone_to_bonemeal");
            grind(Ingredient.of(Items.BONE_BLOCK), Items.BONE_MEAL, 9, 12, "bone_block_to_bonemeal");

            GrindingRecipeBuilder.grinding(Ingredient.of(Items.WHEAT), ModItems.FLOUR.get())
                    .time(12)
                    .save(this.output, "wheat_to_flour");

            grind(Ingredient.of(Items.BLAZE_ROD), Items.BLAZE_POWDER, 4, 8, "blaze_rod_to_powder");
            grind(Ingredient.of(Items.BREEZE_ROD), Items.WIND_CHARGE, 5, 8, "breeze_rod_to_wind_charge");

            GrindingRecipeBuilder.grinding(Ingredient.of(Items.COBBLESTONE), Items.GRAVEL, 1)
                    .secondary(Items.FLINT, 1, 10)
                    .time(8)
                    .save(this.output, "cobblestone_to_gravel");
            GrindingRecipeBuilder.grinding(Ingredient.of(Items.GRAVEL), Items.SAND, 1)
                    .secondary(Items.FLINT, 1, 15)
                    .time(8)
                    .save(this.output, "gravel_to_sand");
            grind(Ingredient.of(Items.STONE), Items.COBBLESTONE, 1, 8, "stone_to_cobblestone");
            grind(Ingredient.of(Items.COBBLED_DEEPSLATE), Items.GRAVEL, 1, 8, "cobbled_deepslate_to_gravel");
            grind(Ingredient.of(Items.NETHERRACK), Items.NETHER_BRICK, 1, 8, "netherrack_to_nether_brick");

            grind(Ingredient.of(Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE), Items.SAND, 2, 8, "sandstone_to_sand");
            grind(Ingredient.of(Items.RED_SANDSTONE, Items.CHISELED_RED_SANDSTONE, Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE), Items.RED_SAND, 2, 8, "red_sandstone_to_sand");

            grind(tag(ItemTags.WOOL), Items.STRING, 4, 8, "wool_to_string");

            grind(Ingredient.of(Items.BRICKS), Items.BRICK, 4, 8, "bricks_to_brick");
            grind(Ingredient.of(Items.NETHER_BRICKS), Items.NETHER_BRICK, 4, 8, "nether_bricks_to_nether_brick");
            grind(Ingredient.of(Items.CLAY), Items.CLAY_BALL, 4, 8, "clay_to_clay_ball");
            grind(Ingredient.of(Items.GLOWSTONE), Items.GLOWSTONE_DUST, 4, 8, "glowstone_to_dust");
            grind(Ingredient.of(Items.HONEYCOMB_BLOCK), Items.HONEYCOMB, 4, 8, "honeycomb_block_to_honeycomb");
            grind(Ingredient.of(Items.DRIPSTONE_BLOCK), Items.POINTED_DRIPSTONE, 4, 8, "dripstone_block_to_pointed");
            grind(Ingredient.of(Items.PRISMARINE), Items.PRISMARINE_SHARD, 4, 12, "prismarine_to_shard");
            grind(Ingredient.of(Items.PRISMARINE_BRICKS), Items.PRISMARINE_SHARD, 9, 12, "prismarine_bricks_to_shard");
            grind(Ingredient.of(Items.QUARTZ_BLOCK), Items.QUARTZ, 4, 8, "quartz_block_to_quartz");

            grind(Ingredient.of(Items.FLINT), Items.GUNPOWDER, 1, 12, "flint_to_gunpowder");
            grind(Ingredient.of(Items.SOUL_SOIL), Items.SOUL_SAND, 1, 8, "soul_soil_to_soul_sand");
            grind(Ingredient.of(Items.SUGAR_CANE), Items.SUGAR, 2, 8, "sugar_cane_to_sugar");

            GrindingRecipeBuilder.grinding(Ingredient.of(Items.RAW_IRON), Items.IRON_NUGGET, 12)
                    .secondary(Items.IRON_NUGGET, 3, 25)
                    .time(16)
                    .save(this.output, "raw_iron_to_nuggets");
            GrindingRecipeBuilder.grinding(Ingredient.of(Items.RAW_GOLD), Items.GOLD_NUGGET, 12)
                    .secondary(Items.GOLD_NUGGET, 3, 25)
                    .time(16)
                    .save(this.output, "raw_gold_to_nuggets");
            GrindingRecipeBuilder.grinding(Ingredient.of(Items.RAW_COPPER), Items.COPPER_INGOT, 1)
                    .secondary(Items.COPPER_INGOT, 1, 25)
                    .time(16)
                    .save(this.output, "raw_copper_to_ingots");
        }

        private void buildPressingRecipes() {
            PressingRecipeBuilder.pressing(Ingredient.of(Items.WHEAT_SEEDS))
                    .inputCount(12)
                    .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                    .save(this.output, "seeds_to_oil");
            PressingRecipeBuilder.pressing(Ingredient.of(Items.MELON_SEEDS))
                    .inputCount(12)
                    .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                    .save(this.output, "melon_seeds_to_oil");
            PressingRecipeBuilder.pressing(Ingredient.of(Items.PUMPKIN_SEEDS))
                    .inputCount(12)
                    .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                    .save(this.output, "pumpkin_seeds_to_oil");
            PressingRecipeBuilder.pressing(Ingredient.of(Items.BEETROOT_SEEDS))
                    .inputCount(12)
                    .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                    .save(this.output, "beetroot_seeds_to_oil");
            PressingRecipeBuilder.pressing(Ingredient.of(Items.TORCHFLOWER_SEEDS))
                    .inputCount(12)
                    .fluidResult(HorsePowerMod.id("seed_oil"), 250)
                    .save(this.output, "torchflower_seeds_to_oil");

            PressingRecipeBuilder.pressing(Ingredient.of(Items.SUGAR_CANE))
                    .inputCount(3)
                    .result(Items.PAPER, 3)
                    .save(this.output, "sugar_cane_to_paper");

            // Small flowers = 4 dye, tall flowers = 8 dye.
            press(Items.DANDELION, Items.YELLOW_DYE, 4, "dandelion_to_dye");
            press(Items.POPPY, Items.RED_DYE, 4, "poppy_to_dye");
            press(Items.BLUE_ORCHID, Items.LIGHT_BLUE_DYE, 4, "blue_orchid_to_dye");
            press(Items.ALLIUM, Items.MAGENTA_DYE, 4, "allium_to_dye");
            press(Items.AZURE_BLUET, Items.LIGHT_GRAY_DYE, 4, "azure_bluet_to_dye");
            press(Items.RED_TULIP, Items.RED_DYE, 4, "red_tulip_to_dye");
            press(Items.ORANGE_TULIP, Items.ORANGE_DYE, 4, "orange_tulip_to_dye");
            press(Items.WHITE_TULIP, Items.WHITE_DYE, 4, "white_tulip_to_dye");
            press(Items.PINK_TULIP, Items.PINK_DYE, 4, "pink_tulip_to_dye");
            press(Items.OXEYE_DAISY, Items.LIGHT_GRAY_DYE, 4, "oxeye_daisy_to_dye");
            press(Items.CORNFLOWER, Items.BLUE_DYE, 4, "cornflower_to_dye");
            press(Items.LILY_OF_THE_VALLEY, Items.WHITE_DYE, 4, "lily_of_the_valley_to_dye");
            press(Items.WITHER_ROSE, Items.BLACK_DYE, 4, "wither_rose_to_dye");
            press(Items.TORCHFLOWER, Items.ORANGE_DYE, 4, "torchflower_to_dye");
            press(Items.OPEN_EYEBLOSSOM, Items.GRAY_DYE, 4, "open_eyeblossom_to_dye");
            press(Items.SUNFLOWER, Items.YELLOW_DYE, 8, "sunflower_to_dye");
            press(Items.LILAC, Items.MAGENTA_DYE, 8, "lilac_to_dye");
            press(Items.ROSE_BUSH, Items.RED_DYE, 8, "rose_bush_to_dye");
            press(Items.PEONY, Items.PINK_DYE, 8, "peony_to_dye");
            press(Items.PITCHER_PLANT, Items.CYAN_DYE, 8, "pitcher_plant_to_dye");

            press(Items.HONEY_BOTTLE, Items.SUGAR, 4, "honey_to_sugar");
            press(Items.HONEYCOMB, Items.HONEY_BOTTLE, 1, "honeycomb_to_honey");

            press(Items.CACTUS, Items.GREEN_DYE, 4, "cactus_to_dye");

            press(Items.WET_SPONGE, Items.SPONGE, 1, "wet_sponge_to_sponge");
            press(Items.MUD, Items.DIRT, 1, "mud_to_dirt");

            PressingRecipeBuilder.pressing(Ingredient.of(Items.ICE))
                    .inputCount(1)
                    .fluidResult(Fluids.WATER, 1000)
                    .save(this.output, "ice_to_water");
            PressingRecipeBuilder.pressing(Ingredient.of(Items.SNOW_BLOCK))
                    .inputCount(1)
                    .fluidResult(Fluids.WATER, 1000)
                    .save(this.output, "snow_to_water");
            PressingRecipeBuilder.pressing(Ingredient.of(Items.SNOWBALL))
                    .inputCount(4)
                    .fluidResult(Fluids.WATER, 1000)
                    .save(this.output, "snowball_to_water");
            PressingRecipeBuilder.pressing(Ingredient.of(Items.PACKED_ICE))
                    .inputCount(1)
                    .fluidResult(Fluids.WATER, 4000)
                    .save(this.output, "packed_ice_to_water");

            press(Items.MAGMA_BLOCK, Items.MAGMA_CREAM, 4, "magma_block_to_cream");

            PressingRecipeBuilder.pressing(Ingredient.of(Items.POINTED_DRIPSTONE))
                    .inputCount(4)
                    .fluidResult(Fluids.WATER, 500)
                    .save(this.output, "pointed_dripstone_to_water");

            PressingRecipeBuilder.pressing(Ingredient.of(Items.KELP))
                    .inputCount(8)
                    .result(Items.DRIED_KELP, 4)
                    .save(this.output, "kelp_to_dried_kelp");

            PressingRecipeBuilder.pressing(Ingredient.of(Items.BONE_MEAL))
                    .inputCount(1)
                    .fluidInput(net.neoforged.neoforge.common.NeoForgeMod.MILK.value(), 1000)
                    .result(Items.SLIME_BALL, 1)
                    .save(this.output, "milk_to_slimeball");
        }

        private void chop(Ingredient input, Item result, int count, int time, String name) {
            ChoppingRecipeBuilder.chopping(input, result, count)
                    .time(time)
                    .save(this.output, name);
        }

        private void grind(Ingredient input, Item result, int count, int time, String name) {
            GrindingRecipeBuilder.grinding(input, result, count)
                    .time(time)
                    .save(this.output, name);
        }

        private void press(Item input, Item result, int count, String name) {
            PressingRecipeBuilder.pressing(Ingredient.of(input))
                    .inputCount(1)
                    .result(result, count)
                    .save(this.output, name);
        }

        private static ResourceKey<Recipe<?>> recipeKey(String path) {
            return ResourceKey.create(Registries.RECIPE, HorsePowerMod.id(path));
        }
    }
}
