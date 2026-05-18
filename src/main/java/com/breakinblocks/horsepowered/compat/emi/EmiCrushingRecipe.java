package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
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
        super(category, null, 78, heightFor(recipe));
        this.time = recipe.getTime();
        this.hungerCost = recipe.getHungerCost();
        this.inputs.add(EmiIngredient.of(recipe.getIngredient()));
        this.outputs.add(EmiStack.of(recipe.getResult()));
    }

    private static int heightFor(CrushingRecipe recipe) {
        return recipe.getHungerCost() > 0.0F ? 38 : 28;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addFillingArrow(26, 1, 10000);
        widgets.addSlot(outputs.get(0), 60, 0).recipeContext(this);

        int strikes = time * HorsePowerConfig.crushingMultiplier.get();
        widgets.addText(
                Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.strikes", strikes),
                26, 20, 0x808080, false);

        if (hungerCost > 0.0F) {
            widgets.addText(
                    Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.hunger",
                            String.format("%.2f", hungerCost)),
                    26, 30, 0x808080, false);
        }
    }
}
