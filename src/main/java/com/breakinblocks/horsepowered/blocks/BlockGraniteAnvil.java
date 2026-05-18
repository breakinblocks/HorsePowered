package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.Configs;
import com.breakinblocks.horsepowered.blockentity.GraniteAnvilBlockEntity;
import com.breakinblocks.horsepowered.recipes.CrushingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockGraniteAnvil extends BlockHPBase {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public BlockGraniteAnvil(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GraniteAnvilBlockEntity anvil) {
            ItemStack held = player.getItemInHand(hand);

            if (held.is(ItemTags.PICKAXES) && held.isCorrectToolForDrops(state) && anvil.canWork()) {
                if (!level.isClientSide) {
                    float recipeHunger = anvil.getRecipe().map(CrushingRecipe::getHungerCost).orElse(0.0F);
                    anvil.crush(player, held);
                    if (Configs.shouldDamageGraniteAnvilPickaxe.get()) {
                        held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                    }
                    player.causeFoodExhaustion(Configs.graniteAnvilExhaustion.get().floatValue() + recipeHunger);
                    playStrikeFeedback(level, pos);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (!held.isEmpty() && anvil.getItem(0).isEmpty() && anvil.isItemValidForSlot(0, held)) {
                if (!level.isClientSide) {
                    ItemStack placed = held.copy();
                    placed.setCount(1);
                    anvil.setItem(0, placed);
                    held.shrink(1);
                    playPlaceFeedback(level, pos);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    private static void playPlaceFeedback(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.9F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    6, 0.2, 0.05, 0.2, 0.0);
        }
    }

    private static void playStrikeFeedback(Level level, BlockPos pos) {
        float pitch = 0.85F + level.getRandom().nextFloat() * 0.20F;
        level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.9F, pitch);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    8, 0.25, 0.1, 0.25, 0.0);
        }
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GraniteAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
