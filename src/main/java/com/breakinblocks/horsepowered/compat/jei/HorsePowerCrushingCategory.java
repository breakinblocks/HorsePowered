package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class HorsePowerCrushingCategory implements IRecipeCategory<CrushingRecipe> {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 66;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;
    private final List<ItemStack> pickaxes;

    public HorsePowerCrushingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.GRANITE_ANVIL.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("gui.horsepowered.jei.crushing");

        this.pickaxes = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(stack -> stack.is(ItemTags.PICKAXES))
                .filter(stack -> stack.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()))
                .toList();
    }

    @Override
    public RecipeType<CrushingRecipe> getRecipeType() {
        return HorsePowerPlugin.CRUSHING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrushingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 42, 1)
                .addItemStacks(pickaxes)
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.INPUT, 10, 26)
                .addIngredients(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 26)
                .addItemStack(recipe.getResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(CrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 38, 26);

        Font font = Minecraft.getInstance().font;
        int strikes = recipe.getTime() * Configs.crushingMultiplier.get();
        Component strikeText = Component.translatable("gui.horsepowered.jei.strikes", strikes);
        int strikeWidth = font.width(strikeText);
        guiGraphics.drawString(font, strikeText, (WIDTH - strikeWidth) / 2, 48, 0x808080, false);

        if (recipe.getHungerCost() > 0.0F) {
            Component hungerText = Component.translatable("gui.horsepowered.jei.hunger",
                    String.format("%.2f", recipe.getHungerCost()));
            int hungerWidth = font.width(hungerText);
            guiGraphics.drawString(font, hungerText, (WIDTH - hungerWidth) / 2, 58, 0x808080, false);
        }
    }
}
