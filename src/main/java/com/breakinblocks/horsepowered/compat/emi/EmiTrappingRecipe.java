package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class EmiTrappingRecipe extends BasicEmiRecipe {

    private final int time;
    private final Optional<TagKey<Biome>> biome;
    private final boolean waterlogged;

    public EmiTrappingRecipe(EmiRecipeCategory category, TrappingRecipe recipe) {
        super(category, recipe.getId(), 160, heightFor(recipe));
        this.time = recipe.getTime();
        this.biome = recipe.getBiome();
        this.waterlogged = recipe.isWaterlogged();
        this.inputs.add(EmiIngredient.of(recipe.getBait()));
        this.outputs.add(EmiStack.of(outputDisplayStack(recipe)));
    }

    private static int heightFor(TrappingRecipe recipe) {
        int lines = 2;
        if (recipe.getBiome().isPresent()) lines++;
        if (recipe.isWaterlogged()) lines++;
        return 26 + lines * 10;
    }

    private static ItemStack outputDisplayStack(TrappingRecipe recipe) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(recipe.getEntityId());
        SpawnEggItem egg = type == null ? null : SpawnEggItem.byId(type);
        return egg != null ? new ItemStack(egg) : new ItemStack(Items.EGG);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 4);
        widgets.addFillingArrow(26, 5, 10000);
        widgets.addSlot(outputs.get(0), 140, 4).recipeContext(this);

        int seconds = Math.max(1, time / 20);
        int y = 26;
        widgets.addText(
                Component.translatable("gui.horsepowered.jei.trap_time", seconds + "s"),
                0, y, 0x808080, false);
        y += 10;
        widgets.addText(
                Component.translatable("gui.horsepowered.jei.trap_chance"),
                0, y, 0x808080, false);
        y += 10;

        if (biome.isPresent()) {
            widgets.addText(
                    Component.translatable("gui.horsepowered.jei.trap_biome",
                            biomeLabel(biome.get().location())),
                    0, y, 0x6688AA, false);
            y += 10;
        }
        if (waterlogged) {
            widgets.addText(
                    Component.translatable("gui.horsepowered.jei.trap_waterlogged"),
                    0, y, 0x6688AA, false);
        }
    }

    private static String biomeLabel(ResourceLocation id) {
        String key = "biome." + id.getNamespace() + "." + id.getPath();
        String resolved = Component.translatable(key).getString();
        if (!resolved.equals(key)) return resolved;
        String path = id.getPath();
        if (path.startsWith("is_")) path = path.substring(3);
        path = path.replace('_', ' ');
        return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
}
