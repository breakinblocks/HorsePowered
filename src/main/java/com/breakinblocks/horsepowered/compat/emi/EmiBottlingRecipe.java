package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

public class EmiBottlingRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 106;

    private final int fluidAmount;

    public EmiBottlingRecipe(EmiRecipeCategory category, BottlingRecipe recipe) {
        super(category, null, WIDTH, 36);
        FluidStack fluid = recipe.getFluid();
        this.fluidAmount = fluid.getAmount();
        this.inputs.add(EmiIngredient.of(recipe.getContainer()));
        this.inputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
        this.outputs.add(EmiStack.of(recipe.getResult()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addSlot(inputs.get(1), 22, 0);
        widgets.addFillingArrow(48, 1, 10000);
        widgets.addSlot(outputs.get(0), 82, 0).recipeContext(this);

        widgets.addText(Component.literal(fluidAmount + " mB"), 22, 20, 0x808080, false);
    }
}
