package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

// TODO: Update to non-deprecated JEI API when available
@SuppressWarnings("removal")
public class HorsePowerChoppingCategory extends BaseHPCategory<ChoppingRecipe> {

    private static final int WIDTH = 82;
    private static final int HEIGHT = 36;

    public HorsePowerChoppingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.CHOPPER.get(), "chopping");
    }

    @Override
    public RecipeType<ChoppingRecipe> getRecipeType() {
        return HorsePowerPlugin.CHOPPING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ChoppingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredients(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                .addItemStack(recipe.createResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(ChoppingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 26, 1);

        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chops", recipe.getTime());
        guiGraphics.text(Minecraft.getInstance().font, timeText, 26, 24, 0x808080, false);
    }
}
