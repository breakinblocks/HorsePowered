package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
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
import net.minecraft.world.level.block.Block;

public class HorsePowerGrindingCategory implements IRecipeCategory<GrindstoneRecipe> {

    private final RecipeType<GrindstoneRecipe> recipeType;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;

    public HorsePowerGrindingCategory(IGuiHelper guiHelper, RecipeType<GrindstoneRecipe> recipeType, Block iconBlock, String titleKey) {
        this.recipeType = recipeType;
        this.background = guiHelper.createBlankDrawable(100, 36);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(iconBlock));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.drawableBuilder(new ResourceLocation("jei", "textures/jei/gui/gui_vanilla.png"), 82, 128, 24, 17).build();
        this.title = Component.translatable(titleKey);
    }

    @Override
    public RecipeType<GrindstoneRecipe> getRecipeType() {
        return recipeType;
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
    public void setRecipe(IRecipeLayoutBuilder builder, GrindstoneRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredients(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 1)
                .addItemStack(recipe.getResult())
                .setBackground(slot, -1, -1);

        if (!recipe.getSecondary().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 1)
                    .addItemStack(recipe.getSecondary())
                    .setBackground(slot, -1, -1)
                    .addTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.translatable("gui.horsepowered.jei.chance",
                                recipe.getSecondaryChance()));
                    });
        }
    }

    @Override
    public void draw(GrindstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 26, 1);

        Component timeText = Component.translatable("gui.horsepowered.jei.time", recipe.getTime());
        guiGraphics.drawString(Minecraft.getInstance().font, timeText, 1, 24, 0x808080, false);

        if (!recipe.getSecondary().isEmpty() && recipe.getSecondaryChance() > 0) {
            String chanceText = recipe.getSecondaryChance() + "%";
            guiGraphics.drawString(Minecraft.getInstance().font, chanceText, 81, 24, 0x808080, false);
        }
    }
}
