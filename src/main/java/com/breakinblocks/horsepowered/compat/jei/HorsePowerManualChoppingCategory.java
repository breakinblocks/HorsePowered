package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// TODO: Update to non-deprecated JEI/Minecraft API when available
@SuppressWarnings({"removal", "deprecation"})
public class HorsePowerManualChoppingCategory extends BaseHPCategory<ChoppingRecipe> {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 60;

    private final List<ItemStack> axes;

    public HorsePowerManualChoppingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.CHOPPING_BLOCK.get(), "manual_chopping");

        this.axes = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.builtInRegistryHolder().is(ItemTags.AXES))
                .map(ItemStack::new)
                .toList();
    }

    @Override
    public RecipeType<ChoppingRecipe> getRecipeType() {
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
                .addIngredients(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 26)
                .addItemStack(recipe.createResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(ChoppingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 38, 26);

        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chops", recipe.getTime());
        int textWidth = Minecraft.getInstance().font.width(timeText);
        guiGraphics.text(Minecraft.getInstance().font, timeText, (WIDTH - textWidth) / 2, 48, 0x808080, false);
    }
}
