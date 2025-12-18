package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class HPChoppingCategory implements IRecipeCategory<ChoppingRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "chopping");

    private static final int WIDTH = 82;
    private static final int HEIGHT = 36;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;

    public HPChoppingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.CHOPPER.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.drawableBuilder(ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/gui/gui_vanilla.png"), 82, 128, 24, 17).build();
        this.title = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chopping");
    }

    @Override
    public RecipeType<ChoppingRecipe> getRecipeType() {
        return HorsePowerPlugin.CHOPPING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ChoppingRecipe recipe, IFocusGroup focuses) {
        // Input slot - left side
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredients(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        // Output slot - right side
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                .addItemStack(recipe.getResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(ChoppingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw arrow between input and output
        arrow.draw(guiGraphics, 26, 1);

        // Draw chop count (time = number of chops)
        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chops", recipe.getTime());
        guiGraphics.drawString(Minecraft.getInstance().font, timeText, 26, 24, 0x808080, false);
    }
}
