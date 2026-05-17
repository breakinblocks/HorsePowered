package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class HorsePowerDryingCategory extends BaseHPCategory<DryingRackRecipe> {

    private static final int WIDTH = 82;
    private static final int HEIGHT = 36;

    public HorsePowerDryingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.DRYING_RACK.get(), "drying");
    }

    @Override
    public IRecipeType<DryingRackRecipe> getRecipeType() {
        return HorsePowerPlugin.DRYING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRackRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .add(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                .add(recipe.createResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(DryingRackRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 26, 1);

        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.time", formatTime(recipe.getTime()));
        int textWidth = Minecraft.getInstance().font.width(timeText);
        guiGraphics.text(Minecraft.getInstance().font, timeText, (WIDTH - textWidth) / 2, 24, 0x808080, false);
    }

    private static String formatTime(int ticks) {
        int totalSeconds = ticks / 20;
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (seconds == 0) return minutes + "m";
        return minutes + "m " + seconds + "s";
    }
}
