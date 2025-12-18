package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.recipes.PressRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
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

import java.util.Arrays;

public class HorsePowerPressCategory implements IRecipeCategory<PressRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "pressing");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;

    public HorsePowerPressCategory(IGuiHelper guiHelper) {
        // Use blank background - we'll draw slots and arrow programmatically
        this.background = guiHelper.createBlankDrawable(82, 50);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PRESS.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.drawableBuilder(new ResourceLocation("jei", "textures/jei/gui/gui_vanilla.png"), 82, 128, 24, 17).build();
        this.title = Component.translatable("gui." + Reference.MODID + ".jei.pressing");
    }

    @Override
    public RecipeType<PressRecipe> getRecipeType() {
        return HorsePowerPlugin.PRESSING_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PressRecipe recipe, IFocusGroup focuses) {
        // Input slot with count - left side
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(Arrays.stream(recipe.getIngredient().getItems())
                        .map(stack -> {
                            ItemStack copy = stack.copy();
                            copy.setCount(recipe.getInputCount());
                            return copy;
                        })
                        .toList())
                .setBackground(slot, -1, -1);

        // Output - either item or fluid
        if (recipe.hasFluidOutput()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                    .setFluidRenderer(recipe.getFluidResult().getAmount(), false, 16, 32)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.getFluidResult());
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                    .addItemStack(recipe.getResult())
                    .setBackground(slot, -1, -1);
        }
    }

    @Override
    public void draw(PressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw arrow between input and output
        arrow.draw(guiGraphics, 26, 1);

        // Draw input count if more than 1
        if (recipe.getInputCount() > 1) {
            String countText = "x" + recipe.getInputCount();
            guiGraphics.drawString(Minecraft.getInstance().font, countText, 1, 24, 0x808080, false);
        }

        // Draw fluid amount if fluid output
        if (recipe.hasFluidOutput()) {
            String fluidText = recipe.getFluidResult().getAmount() + " mB";
            guiGraphics.drawString(Minecraft.getInstance().font, fluidText, 55, 36, 0x808080, false);
        }
    }
}
