package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.blocks.ModBlocks;
import com.breakinblocks.horsepowered.recipes.TrappingRecipe;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

public class HorsePowerTrappingCategory implements IRecipeCategory<TrappingRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(HorsePowerMod.MOD_ID, "trapping");

    private static final int WIDTH = 160;
    private static final int HEIGHT = 94;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;

    public HorsePowerTrappingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ANIMAL_TRAP.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trapping");
    }

    @Override
    public RecipeType<TrappingRecipe> getRecipeType() {
        return HorsePowerPlugin.TRAPPING_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, TrappingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 50, 1)
                .addItemStack(new ItemStack(ModBlocks.ANIMAL_TRAP.get()))
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 22)
                .addIngredients(recipe.getBait())
                .setBackground(slot, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 22)
                .addItemStack(outputDisplayStack(recipe))
                .setBackground(slot, -1, -1);
    }

    private static ItemStack outputDisplayStack(TrappingRecipe recipe) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(recipe.getEntityId());
        SpawnEggItem egg = type == null ? null : SpawnEggItem.byId(type);
        if (egg != null) return new ItemStack(egg);
        return new ItemStack(Items.EGG);
    }

    @Override
    public void draw(TrappingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 24, 22);
        var font = Minecraft.getInstance().font;
        int y = 42;

        int seconds = Math.max(1, recipe.getTime() / 20);
        Component timeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_time", seconds + "s");
        guiGraphics.drawString(font, timeText, (WIDTH - font.width(timeText)) / 2, y, 0xFF808080, false);
        y += 10;

        Component chanceText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_chance");
        guiGraphics.drawString(font, chanceText, (WIDTH - font.width(chanceText)) / 2, y, 0xFF808080, false);
        y += 10;

        Component baitText = baitConsumedLabel(recipe);
        guiGraphics.drawString(font, baitText, (WIDTH - font.width(baitText)) / 2, y, 0xFF808080, false);
        y += 10;

        if (recipe.getBiome().isPresent()) {
            Component biomeText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_biome",
                    biomeLabel(recipe.getBiome().get().location()));
            guiGraphics.drawString(font, biomeText, (WIDTH - font.width(biomeText)) / 2, y, 0xFF6688AA, false);
            y += 10;
        }

        if (recipe.isWaterlogged()) {
            Component waterText = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_waterlogged");
            guiGraphics.drawString(font, waterText, (WIDTH - font.width(waterText)) / 2, y, 0xFF6688AA, false);
        }
    }

    private static Component baitConsumedLabel(TrappingRecipe recipe) {
        if (!recipe.isBaitConsumed()) {
            return Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_bait_consumed_none");
        }
        return Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.trap_bait_consumed_chance",
                formatChance(recipe.getBaitConsumeChance()));
    }

    private static String formatChance(double chance) {
        if (chance == Math.floor(chance)) {
            return String.format("%d", (long) chance);
        }
        return String.format("%.2f", chance);
    }

    private static String biomeLabel(net.minecraft.resources.ResourceLocation id) {
        String key = "biome." + id.getNamespace() + "." + id.getPath();
        Component translated = Component.translatable(key);
        String resolved = translated.getString();
        if (!resolved.equals(key)) return resolved;
        String path = id.getPath();
        if (path.startsWith("is_")) path = path.substring(3);
        path = path.replace('_', ' ');
        return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
}
