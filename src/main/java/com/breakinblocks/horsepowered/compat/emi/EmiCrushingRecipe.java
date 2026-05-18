package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;

public class EmiCrushingRecipe extends BasicEmiRecipe {

    private final int time;
    private final float hungerCost;

    public EmiCrushingRecipe(EmiRecipeCategory category, CrushingRecipe recipe) {
        super(category, recipe.getId(), 78, recipe.getHungerCost() > 0.0F ? 38 : 28);
        this.time = recipe.getTime();
        this.hungerCost = recipe.getHungerCost();
        this.inputs.add(EmiIngredient.of(recipe.getIngredient()));
        this.outputs.add(EmiStack.of(recipe.getResult()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addFillingArrow(26, 1, 10000);
        widgets.addSlot(outputs.get(0), 60, 0).recipeContext(this);

        int strikes = time * Configs.crushingMultiplier.get();
        widgets.addText(
                Component.translatable("gui.horsepowered.jei.strikes", strikes),
                26, 20, 0x808080, false);

        if (hungerCost > 0.0F) {
            widgets.addText(
                    Component.translatable("gui.horsepowered.jei.hunger",
                            String.format("%.2f", hungerCost)),
                    26, 30, 0x808080, false);
        }
    }
}
