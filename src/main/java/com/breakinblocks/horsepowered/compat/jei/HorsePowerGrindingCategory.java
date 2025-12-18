package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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

public class HorsePowerGrindingCategory implements IRecipeCategory<GrindstoneRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "grinding");

    private static final int WIDTH = 100;
    private static final int HEIGHT = 36;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;

    public HorsePowerGrindingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.GRINDSTONE.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.grinding");
    }

    @Override
    public RecipeType<GrindstoneRecipe> getRecipeType() {
        return HorsePowerPlugin.GRINDING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, GrindstoneRecipe recipe, IFocusGroup focuses) {
        // Input slot - left side
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredients(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        // Main output slot - right side
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                .addItemStack(recipe.getResult())
                .setBackground(slot, -1, -1);

        // Secondary output slot (if exists) - below main output
        if (!recipe.getSecondary().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 1)
                    .addItemStack(recipe.getSecondary())
                    .setBackground(slot, -1, -1)
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chance",
                                recipe.getSecondaryChance()));
                    });
        }
    }

    @Override
    public void draw(GrindstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw arrow between input and output
        arrow.draw(guiGraphics, 26, 1);

        // Draw time info below
        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.time", recipe.getTime());
        guiGraphics.drawString(Minecraft.getInstance().font, timeText, 1, 24, 0x808080, false);

        // Draw secondary chance if applicable
        if (!recipe.getSecondary().isEmpty() && recipe.getSecondaryChance() > 0) {
            String chanceText = recipe.getSecondaryChance() + "%";
            guiGraphics.drawString(Minecraft.getInstance().font, chanceText, 81, 24, 0x808080, false);
        }
    }
}
