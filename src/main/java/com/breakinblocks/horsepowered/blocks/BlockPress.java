package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockPress extends BlockHPBase {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BlockPress(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof PressBlockEntity press) {
            ItemStack stack = player.getItemInHand(hand);
            if (FluidUtil.getFluidHandler(stack).isPresent()) {
                if (level.isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                if (FluidUtil.interactWithFluidHandler(player, hand, press.getFluidHandler())) {
                    press.setChanged();
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }

            BottlingRecipe bottling = findBottlingRecipe(level, press, stack);
            if (bottling != null) {
                if (level.isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                if (applyBottling(press, bottling, stack, player, hand)) {
                    press.setChanged();
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Nullable
    private static BottlingRecipe findBottlingRecipe(Level level, PressBlockEntity press, ItemStack stack) {
        if (stack.isEmpty()) return null;
        List<BottlingRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(HPRecipes.BOTTLING_TYPE.get())
                .stream()
                .filter(BottlingRecipe::isValid)
                .toList();

        for (BottlingRecipe recipe : recipes) {
            if (recipe.matchesResult(stack) && canEmpty(press.getInputTank(), recipe.getFluid())) {
                return recipe;
            }
        }
        for (BottlingRecipe recipe : recipes) {
            if (recipe.matchesContainer(stack) && canFill(press.getOutputTank(), recipe.getFluid())) {
                return recipe;
            }
        }
        return null;
    }

    private static boolean canFill(FluidTank outputTank, FluidStack fluid) {
        FluidStack held = outputTank.getFluid();
        return held.isFluidEqual(fluid) && held.getAmount() >= fluid.getAmount();
    }

    private static boolean canEmpty(FluidTank inputTank, FluidStack fluid) {
        return inputTank.fill(fluid.copy(), IFluidHandler.FluidAction.SIMULATE) == fluid.getAmount();
    }

    private static boolean applyBottling(PressBlockEntity press, BottlingRecipe recipe, ItemStack stack,
                                         Player player, InteractionHand hand) {
        boolean emptying = recipe.matchesResult(stack);
        ItemStack given;
        if (emptying) {
            if (!canEmpty(press.getInputTank(), recipe.getFluid())) return false;
            press.getInputTank().fill(recipe.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
            given = recipe.getEmptyContainer();
        } else {
            if (!canFill(press.getOutputTank(), recipe.getFluid())) return false;
            press.getOutputTank().drain(recipe.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
            given = recipe.getResult().copy();
        }

        if (player.getAbilities().instabuild) return true;

        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, given);
        } else if (!player.getInventory().add(given)) {
            player.drop(given, false);
        }
        return true;
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
        // No special action needed
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PressBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return checkType(type, ModBlocks.PRESS_BE.get(), HPBlockEntityHorseBase::clientTick);
        }
        return checkType(type, ModBlocks.PRESS_BE.get(), HPBlockEntityHorseBase::serverTick);
    }
}
