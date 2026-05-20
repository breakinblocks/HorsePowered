package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Optional;

public class HorsePowerTrappingCategory extends BaseHPCategory<TrappingRecipe> {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 84;

    public HorsePowerTrappingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.ANIMAL_TRAP.get(), "trapping");
    }

    @Override
    public IRecipeType<TrappingRecipe> getRecipeType() {
        return HorsePowerPlugin.TRAPPING_TYPE;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TrappingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 72, 1)
                .addItemStack(new ItemStack(ModBlocks.ANIMAL_TRAP.get()))
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.INPUT, 20, 22)
                .add(recipe.getBait())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 22)
                .addItemStack(outputDisplayStack(recipe))
                .setBackground(slot, -1, -1);
    }

    private static ItemStack outputDisplayStack(TrappingRecipe recipe) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(recipe.getEntityId());
        if (type == null) return new ItemStack(Items.EGG);
        Optional<Holder<Item>> egg = SpawnEggItem.byId(type);
        return egg.map(h -> new ItemStack(h.value())).orElse(new ItemStack(Items.EGG));
    }

    @Override
    public void draw(TrappingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 50, 22);

        Font font = Minecraft.getInstance().font;
        int y = 42;

        int seconds = Math.max(1, recipe.getTime() / 20);
        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_time", seconds + "s");
        guiGraphics.text(font, timeText, (WIDTH - font.width(timeText)) / 2, y, 0xFF808080, false);
        y += 10;

        Component chanceText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_chance");
        guiGraphics.text(font, chanceText, (WIDTH - font.width(chanceText)) / 2, y, 0xFF808080, false);
        y += 10;

        if (recipe.getBiome().isPresent()) {
            Component biomeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_biome",
                    biomeLabel(recipe.getBiome().get().location()));
            guiGraphics.text(font, biomeText, (WIDTH - font.width(biomeText)) / 2, y, 0xFF6688AA, false);
            y += 10;
        }

        if (recipe.isWaterlogged()) {
            Component waterText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_waterlogged");
            guiGraphics.text(font, waterText, (WIDTH - font.width(waterText)) / 2, y, 0xFF6688AA, false);
        }
    }

    private static String biomeLabel(Identifier id) {
        String key = "biome." + id.getNamespace() + "." + id.getPath();
        String resolved = Component.translatable(key).getString();
        if (!resolved.equals(key)) return resolved;
        String path = id.getPath();
        if (path.startsWith("is_")) path = path.substring(3);
        path = path.replace('_', ' ');
        return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
}
