package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.HorsePowerMod;
import com.breakinblocks.horsepowered.config.HorsePowerConfig;
import com.breakinblocks.horsepowered.recipes.GrindstoneRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class EmiGrindingRecipe extends BasicEmiRecipe {

    private final int time;
    private final ItemStack secondary;
    private final int secondaryChance;
    private final boolean manual;
    private final float hungerCost;

    public EmiGrindingRecipe(EmiRecipeCategory category, GrindstoneRecipe recipe) {
        super(category, null, widthFor(recipe), heightFor(category, recipe));
        this.time = recipe.getTime();
        this.secondary = recipe.getSecondary();
        this.secondaryChance = recipe.getSecondaryChance();
        this.manual = category == HorsePoweredEmiPlugin.MANUAL_GRINDING;
        this.hungerCost = recipe.getHungerCost();

        this.inputs.add(EmiIngredient.of(recipe.getIngredient()));
        this.outputs.add(EmiStack.of(recipe.getResult()));
        if (!secondary.isEmpty()) {
            this.outputs.add(EmiStack.of(secondary).setChance(secondaryChance / 100f));
        }
    }

    private static int widthFor(GrindstoneRecipe recipe) {
        return recipe.getSecondary().isEmpty() ? 78 : 98;
    }

    private static int heightFor(EmiRecipeCategory category, GrindstoneRecipe recipe) {
        return category == HorsePoweredEmiPlugin.MANUAL_GRINDING && recipe.getHungerCost() > 0.0F ? 38 : 28;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addFillingArrow(26, 1, 10000);
        widgets.addSlot(outputs.get(0), 60, 0).recipeContext(this);

        if (manual) {
            int pointsPerTurn = HorsePowerConfig.pointsPerRotation.get();
            int turns = Math.max(1, (time + pointsPerTurn - 1) / pointsPerTurn);
            widgets.addText(
                    Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.turns", turns),
                    0, 20, 0x808080, false);
        } else {
            widgets.addText(
                    Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.time", time),
                    0, 20, 0x808080, false);
        }

        if (!secondary.isEmpty()) {
            widgets.addSlot(outputs.get(1), 80, 0).recipeContext(this);
            widgets.addText(
                    Component.literal(secondaryChance + "%"),
                    80, 20, 0x808080, false);
        }

        if (manual && hungerCost > 0.0F) {
            widgets.addText(
                    Component.translatable("gui." + HorsePowerMod.MOD_ID + ".jei.hunger",
                            String.format("%.2f", hungerCost)),
                    0, 30, 0x808080, false);
        }
    }
}
