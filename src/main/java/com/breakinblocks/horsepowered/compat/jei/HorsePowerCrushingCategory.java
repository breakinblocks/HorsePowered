package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;

import java.util.List;

public class HorsePowerCrushingCategory implements IRecipeCategory<CrushingRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "crushing");

    private static final int WIDTH = 100;
    private static final int HEIGHT = 66;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;
    private final List<ItemStack> pickaxes;

    public HorsePowerCrushingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.GRANITE_ANVIL.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.crushing");

        this.pickaxes = BuiltInRegistries.ITEM.getTag(ItemTags.PICKAXES)
                .map(tag -> tag.stream()
                        .map(holder -> new ItemStack(holder.value()))
                        .filter(s -> s.getItem() instanceof TieredItem ti
                                && ti.getTier().getIncorrectBlocksForDrops() != Tiers.WOOD.getIncorrectBlocksForDrops())
                        .toList())
                .orElse(List.of());
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

        int strikes = recipe.getTime() * HorsePowerConfig.crushingMultiplier.get();
        Component strikeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.strikes", strikes);
        int strikeWidth = Minecraft.getInstance().font.width(strikeText);
        guiGraphics.drawString(Minecraft.getInstance().font, strikeText, (WIDTH - strikeWidth) / 2, 48, 0x808080, false);

        if (recipe.getHungerCost() > 0.0F) {
            Component hungerText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.hunger",
                    formatHunger(recipe.getHungerCost()));
            int hungerWidth = Minecraft.getInstance().font.width(hungerText);
            guiGraphics.drawString(Minecraft.getInstance().font, hungerText, (WIDTH - hungerWidth) / 2, 58, 0x808080, false);
        }
    }

    private static String formatHunger(float value) {
        return String.format("%.2f", value);
    }
}
