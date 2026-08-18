package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.breakinblocks.horsepowered.blockentity.ModBlockEntities;
import com.breakinblocks.horsepowered.blockentity.PressBlockEntity;
import com.breakinblocks.horsepowered.recipes.BottlingRecipe;
import com.breakinblocks.horsepowered.recipes.HPRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockPress extends BlockHPBase {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BlockPress(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof PressBlockEntity press) {
            if (ItemAccess.forPlayerInteraction(player, hand).getCapability(Capabilities.Fluid.ITEM) != null) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                if (FluidUtil.interactWithFluidHandler(player, hand, pos, press.getFluidHandler())) {
                    press.setChanged();
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }

            if (level instanceof ServerLevel serverLevel) {
                BottlingRecipe bottling = findBottlingRecipe(serverLevel, press, stack);
                if (bottling != null) {
                    return applyBottling(press, bottling, stack, player, hand)
                            ? InteractionResult.SUCCESS
                            : InteractionResult.CONSUME;
                }
            } else if (hasAnyBottlingRecipe(level, stack)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    private static boolean hasAnyBottlingRecipe(Level level, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return ((RecipeManager) level.recipeAccess()).recipeMap().byType(HPRecipes.BOTTLING_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .filter(BottlingRecipe::isValid)
                .anyMatch(recipe -> recipe.matchesResult(stack) || recipe.matchesContainer(stack));
    }

    @Nullable
    private static BottlingRecipe findBottlingRecipe(ServerLevel level, PressBlockEntity press, ItemStack stack) {
        if (stack.isEmpty()) return null;
        List<BottlingRecipe> recipes = ((RecipeManager) level.recipeAccess())
                .recipeMap().byType(HPRecipes.BOTTLING_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .filter(BottlingRecipe::isValid)
                .toList();

        for (BottlingRecipe recipe : recipes) {
            if (recipe.matchesResult(stack) && canEmpty(press, recipe.getFluid())) {
                return recipe;
            }
        }
        for (BottlingRecipe recipe : recipes) {
            if (recipe.matchesContainer(stack) && canFill(press, recipe.getFluid())) {
                return recipe;
            }
        }
        return null;
    }

    private static boolean canFill(PressBlockEntity press, FluidStack fluid) {
        FluidStack held = press.getOutputFluid();
        return FluidStack.isSameFluidSameComponents(held, fluid) && held.getAmount() >= fluid.getAmount();
    }

    private static boolean canEmpty(PressBlockEntity press, FluidStack fluid) {
        FluidStack held = press.getInputFluid();
        if (!held.isEmpty() && !FluidStack.isSameFluidSameComponents(held, fluid)) return false;
        return press.getTankCapacity() - held.getAmount() >= fluid.getAmount();
    }

    private static boolean applyBottling(PressBlockEntity press, BottlingRecipe recipe, ItemStack stack,
                                         Player player, InteractionHand hand) {
        boolean emptying = recipe.matchesResult(stack);
        ItemStack given;
        if (emptying) {
            if (!press.fillInput(recipe.getFluid())) return false;
            given = recipe.getEmptyContainer();
        } else {
            if (!press.drainOutput(recipe.getFluid())) return false;
            given = recipe.createResult();
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PressBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return checkType(type, ModBlockEntities.PRESS.get(), HPBlockEntityHorseBase::clientTick);
        } else {
            return checkType(type, ModBlockEntities.PRESS.get(), HPBlockEntityHorseBase::serverTick);
        }
    }
}
