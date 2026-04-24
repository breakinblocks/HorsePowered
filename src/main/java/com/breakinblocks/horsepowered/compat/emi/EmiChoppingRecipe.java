package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.recipes.ChoppingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;

public class EmiChoppingRecipe extends BasicEmiRecipe {

    private final int time;

    public EmiChoppingRecipe(EmiRecipeCategory category, ChoppingRecipe recipe) {
        super(category, null, 78, 28);
        this.time = recipe.getTime();
        this.inputs.add(EmiIngredient.of(recipe.getIngredient()));
        this.outputs.add(EmiStack.of(recipe.getResult()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addFillingArrow(26, 1, 200);
        widgets.addSlot(outputs.get(0), 60, 0).recipeContext(this);
        widgets.addText(
                Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.chops", time),
                26, 20, 0x808080, false);
    }
}
