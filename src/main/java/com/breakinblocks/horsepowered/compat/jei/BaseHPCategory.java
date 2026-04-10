package com.breakinblocks.horsepowered.compat.jei;

import com.breakinblocks.horsepowered.HorsePowerMod;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Base class for all HorsePowered JEI recipe categories, providing shared constructor
 * boilerplate and common getter implementations.
 */
// TODO: Update to non-deprecated JEI API when available
@SuppressWarnings("removal")
public abstract class BaseHPCategory<T> implements IRecipeCategory<T> {

    protected final IDrawable icon;
    protected final IDrawable slot;
    protected final IDrawable arrow;
    protected final Component title;

    protected BaseHPCategory(IGuiHelper guiHelper, ItemLike iconItem, String translationKey) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(iconItem));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei." + translationKey);
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
