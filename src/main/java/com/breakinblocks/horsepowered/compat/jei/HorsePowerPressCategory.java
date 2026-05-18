package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
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
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class HorsePowerPressCategory extends BaseHPCategory<PressRecipe> {

    private static final int WIDTH = 82;
    private static final int HEIGHT = 50;

    public HorsePowerPressCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.PRESS.get(), "pressing");
    }

    @Override
    public IRecipeType<PressRecipe> getRecipeType() {
        return HorsePowerPlugin.PRESSING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, PressRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(recipe.getIngredient().getValues().stream()
                        .map(Holder::value)
                        .map(item -> {
                            ItemStack stack = new ItemStack(item);
                            stack.setCount(recipe.getInputCount());
                            return stack;
                        })
                        .toList())
                .setBackground(slot, -1, -1);

        recipe.getFluidInput().ifPresent(fluidIn ->
                builder.addSlot(RecipeIngredientRole.INPUT, 1, 22)
                        .setFluidRenderer(fluidIn.amount(), false, 16, 16)
                        .addIngredients(NeoForgeTypes.FLUID_STACK, fluidIn.ingredient().fluids().stream()
                                .map(h -> new FluidStack(h.value(), fluidIn.amount()))
                                .toList()));

        if (recipe.hasFluidOutput()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                    .setFluidRenderer(recipe.getFluidResult().getAmount(), false, 16, 32)
                    .add(NeoForgeTypes.FLUID_STACK, recipe.getFluidResult());
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                    .add(recipe.createResult())
                    .setBackground(slot, -1, -1);
        }
    }

    @Override
    public void draw(PressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 26, 1);

        if (recipe.hasFluidOutput()) {
            Font font = Minecraft.getInstance().font;
            String fluidText = recipe.getFluidResult().getAmount() + " mB";
            int x = WIDTH - font.width(fluidText);
            guiGraphics.text(font, fluidText, x, 36, 0xFF808080, false);
        }
    }
}
