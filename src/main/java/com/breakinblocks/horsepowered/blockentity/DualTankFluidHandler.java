package com.breakinblocks.horsepowered.blockentity;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes two internal tanks as a single directional {@link IFluidHandler}:
 * external insertions route to the input tank; external extractions drain
 * only the output tank. Tank 0 is input (fillable), tank 1 is output (drainable).
 */
public class DualTankFluidHandler implements IFluidHandler {

    public static final int INPUT_INDEX = 0;
    public static final int OUTPUT_INDEX = 1;

    private final FluidTank inputTank;
    private final FluidTank outputTank;

    public DualTankFluidHandler(FluidTank inputTank, FluidTank outputTank) {
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return switch (tank) {
            case INPUT_INDEX -> inputTank.getFluid();
            case OUTPUT_INDEX -> outputTank.getFluid();
            default -> FluidStack.EMPTY;
        };
    }

    @Override
    public int getTankCapacity(int tank) {
        return switch (tank) {
            case INPUT_INDEX -> inputTank.getCapacity();
            case OUTPUT_INDEX -> outputTank.getCapacity();
            default -> 0;
        };
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == INPUT_INDEX && inputTank.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return inputTank.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return outputTank.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return outputTank.drain(maxDrain, action);
    }
}
