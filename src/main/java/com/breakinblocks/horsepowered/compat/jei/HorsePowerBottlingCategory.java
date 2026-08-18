package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class HorsePowerBottlingCategory implements IRecipeCategory<BottlingRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "bottling");

    private static final int WIDTH = 120;
    private static final int HEIGHT = 50;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;

    public HorsePowerBottlingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PRESS.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.bottling");
    }

    @Override
    public RecipeType<BottlingRecipe> getRecipeType() {
        return HorsePowerPlugin.BOTTLING_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
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
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BottlingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .addItemStacks(Arrays.asList(recipe.getContainer().getItems()))
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.CATALYST, 25, 1)
                .setFluidRenderer(recipe.getFluid().getAmount(), false, 16, 32)
                .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getFluid());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 9)
                .addItemStack(recipe.getResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(BottlingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 66, 9);

        Font font = Minecraft.getInstance().font;
        String fluidText = recipe.getFluid().getAmount() + " mB";
        guiGraphics.drawString(font, fluidText, 25, 36, 0xFF808080, false);
    }
}
