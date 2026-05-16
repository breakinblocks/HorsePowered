package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class HorsePowerManualGrindingCategory extends BaseHPCategory<GrindstoneRecipe> {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 46;

    public HorsePowerManualGrindingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.HAND_GRINDSTONE.get(), "manual_grinding");
    }

    @Override
    public IRecipeType<GrindstoneRecipe> getRecipeType() {
        return HorsePowerPlugin.MANUAL_GRINDING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, GrindstoneRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .add(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                .add(recipe.createResult())
                .setBackground(slot, -1, -1);

        if (recipe.getSecondaryTemplate() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 1)
                    .add(recipe.createSecondary())
                    .setBackground(slot, -1, -1)
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chance",
                                recipe.getSecondaryChance()));
                    });
        }
    }

    @Override
    public void draw(GrindstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 26, 1);

        int pointsPerTurn = HorsePowerConfig.pointsPerRotation.get();
        int turns = Math.max(1, (recipe.getTime() + pointsPerTurn - 1) / pointsPerTurn);
        Component turnText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.turns", turns);
        guiGraphics.text(Minecraft.getInstance().font, turnText, 1, 24, 0x808080, false);

        if (recipe.getSecondaryTemplate() != null && recipe.getSecondaryChance() > 0) {
            String chanceText = recipe.getSecondaryChance() + "%";
            guiGraphics.text(Minecraft.getInstance().font, chanceText, 81, 24, 0x808080, false);
        }

        if (recipe.getHungerCost() > 0.0F) {
            Component hungerText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.hunger",
                    String.format("%.2f", recipe.getHungerCost()));
            guiGraphics.text(Minecraft.getInstance().font, hungerText, 1, 34, 0x808080, false);
        }
    }
}
