package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.lib.Reference;
import com.breakinblocks.horsepowered.recipes.DryingRackRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class EmiDryingRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 78;

    private final int time;

    public EmiDryingRecipe(EmiRecipeCategory category, DryingRackRecipe recipe) {
        super(category, recipe.getId(), WIDTH, 36);
        this.time = recipe.getTime();
        this.inputs.add(EmiIngredient.of(recipe.getIngredient()));
        this.outputs.add(EmiStack.of(recipe.getResult()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addFillingArrow(26, 1, time * 50);
        widgets.addSlot(outputs.get(0), 60, 0).recipeContext(this);

        Component text = Component.translatable("gui." + Reference.MODID + ".jei.time", formatTime(time));
        int x = (WIDTH - Minecraft.getInstance().font.width(text)) / 2;
        widgets.addText(text, x, 20, 0x808080, false);
    }

    private static String formatTime(int ticks) {
        float seconds = ticks / 20.0F;
        if (seconds == Math.floor(seconds)) {
            return ((int) seconds) + "s";
        }
        return String.format("%.1fs", seconds);
    }
}
