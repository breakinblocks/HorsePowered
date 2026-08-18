package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HorsePowerBottlingCategory extends BaseHPCategory<BottlingRecipe> {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 50;

    public HorsePowerBottlingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.PRESS.get(), "bottling");
    }

    @Override
    public IRecipeType<BottlingRecipe> getRecipeType() {
        return HorsePowerPlugin.BOTTLING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, BottlingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .add(recipe.getContainer())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 25, 1)
                .setFluidRenderer(recipe.getFluid().getAmount(), false, 16, 32)
                .add(NeoForgeTypes.FLUID_STACK, recipe.getFluid());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 9)
                .add(recipe.createResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(BottlingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 66, 9);

        Font font = Minecraft.getInstance().font;
        String fluidText = recipe.getFluid().getAmount() + " mB";
        guiGraphics.text(font, fluidText, 25, 36, 0xFF808080, false);
    }
}
