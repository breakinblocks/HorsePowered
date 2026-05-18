package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class HorsePowerCrushingCategory extends BaseHPCategory<CrushingRecipe> {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 66;

    private final List<ItemStack> pickaxes;

    public HorsePowerCrushingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.GRANITE_ANVIL.get(), "crushing");

        this.pickaxes = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(stack -> stack.is(ItemTags.PICKAXES))
                .filter(stack -> stack.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()))
                .toList();
    }

    @Override
    public IRecipeType<CrushingRecipe> getRecipeType() {
        return HorsePowerPlugin.CRUSHING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrushingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 42, 1)
                .addItemStacks(pickaxes)
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.INPUT, 10, 26)
                .add(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 26)
                .add(recipe.createResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(CrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 38, 26);

        int strikes = recipe.getTime() * HorsePowerConfig.crushingMultiplier.get();
        Component strikeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.strikes", strikes);
        int strikeWidth = Minecraft.getInstance().font.width(strikeText);
        guiGraphics.text(Minecraft.getInstance().font, strikeText, (WIDTH - strikeWidth) / 2, 48, 0xFF808080, false);

        if (recipe.getHungerCost() > 0.0F) {
            Component hungerText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.hunger",
                    String.format("%.2f", recipe.getHungerCost()));
            int hungerWidth = Minecraft.getInstance().font.width(hungerText);
            guiGraphics.text(Minecraft.getInstance().font, hungerText, (WIDTH - hungerWidth) / 2, 58, 0xFF808080, false);
        }
    }
}
