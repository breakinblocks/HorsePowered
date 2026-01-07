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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// TODO: Update to non-deprecated JEI/Minecraft API when available
@SuppressWarnings({"removal", "deprecation"})
public class HPManualChoppingCategory implements IRecipeCategory<ChoppingRecipe> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "manual_chopping");

    private static final int WIDTH = 100;
    private static final int HEIGHT = 60;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;
    private final List<ItemStack> axes;

    public HPManualChoppingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.CHOPPING_BLOCK.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.manual_chopping");

        // Get all axes from the axes tag
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
        // Axe slot - top center (crafting station, not consumed)
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 42, 1)
                .addItemStacks(axes)
                .setBackground(slot, -1, -1);

        // Input slot - left side
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 26)
                .addIngredients(recipe.getIngredient())
                .setBackground(slot, -1, -1);

        // Output slot - right side
        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 26)
                .addItemStack(recipe.getResult())
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(ChoppingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw arrow between input and output
        arrow.draw(guiGraphics, 38, 26);

        // Draw chop count (time = number of chops)
        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chops", recipe.getTime());
        int textWidth = Minecraft.getInstance().font.width(timeText);
        guiGraphics.drawString(Minecraft.getInstance().font, timeText, (WIDTH - textWidth) / 2, 48, 0x808080, false);
    }
}
