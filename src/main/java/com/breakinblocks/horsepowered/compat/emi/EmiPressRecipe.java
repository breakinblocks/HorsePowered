package com.breakinblocks.horsepowered.compat.emi;

import com.breakinblocks.horsepowered.recipes.PressRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;

public class EmiPressRecipe extends BasicEmiRecipe {

    private final int inputCount;
    private final boolean fluidOutput;
    private final int fluidAmount;

    public EmiPressRecipe(EmiRecipeCategory category, PressRecipe recipe) {
        super(category, null, 78, 36);
        this.inputCount = recipe.getInputCount();
        this.fluidOutput = recipe.hasFluidOutput();
        this.fluidAmount = fluidOutput ? recipe.getFluidResult().getAmount() : 0;

        EmiIngredient input = EmiIngredient.of(recipe.getIngredient());
        if (inputCount > 1) {
            input = EmiIngredient.of(
                    Arrays.stream(recipe.getIngredient().getItems())
                            .map(stack -> {
                                ItemStack copy = stack.copy();
                                copy.setCount(inputCount);
                                return EmiStack.of(copy);
                            })
                            .toList());
        }
        this.inputs.add(input);

        if (fluidOutput) {
            FluidStack fs = recipe.getFluidResult();
            this.outputs.add(EmiStack.of(fs.getFluid(), fs.getAmount()));
        } else {
            this.outputs.add(EmiStack.of(recipe.getResult()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addFillingArrow(26, 1, 200);
        widgets.addSlot(outputs.get(0), 60, 0).recipeContext(this);

        if (inputCount > 1) {
            widgets.addText(
                    Component.literal("x" + inputCount),
                    0, 20, 0x808080, false);
        }
        if (fluidOutput) {
            widgets.addText(
                    Component.literal(fluidAmount + " mB"),
                    54, 20, 0x808080, false);
        }
    }
}
