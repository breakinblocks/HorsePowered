package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
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

import java.util.List;

public class HorsePowerManualChoppingCategory extends BaseHPCategory<ChoppingRecipe> {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 66;

    private final List<ItemStack> axes;

    public HorsePowerManualChoppingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.CHOPPING_BLOCK.get(), "manual_chopping");

        this.axes = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(stack -> stack.is(ItemTags.AXES))
                .toList();
    }

    @Override
    public IRecipeType<ChoppingRecipe> getRecipeType() {
        return HorsePowerPlugin.MANUAL_CHOPPING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ChoppingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 42, 1)
                .addItemStacks(axes)
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.INPUT, 10, 26)
                .add(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 26)
                .add(recipe.createResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(ChoppingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 38, 26);

        int chops = recipe.getTime() * HorsePowerConfig.choppingMultiplier.get();
        Component chopText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chops", chops);
        int chopWidth = Minecraft.getInstance().font.width(chopText);
        guiGraphics.text(Minecraft.getInstance().font, chopText, (WIDTH - chopWidth) / 2, 48, 0x808080, false);

        if (recipe.getHungerCost() > 0.0F) {
            Component hungerText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.hunger",
                    String.format("%.2f", recipe.getHungerCost()));
            int hungerWidth = Minecraft.getInstance().font.width(hungerText);
            guiGraphics.text(Minecraft.getInstance().font, hungerText, (WIDTH - hungerWidth) / 2, 58, 0x808080, false);
        }
    }
}
