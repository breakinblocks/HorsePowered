package com.breakinblocks.horsepowered.blocks;

import com.breakinblocks.horsepowered.blockentity.GeneratorBlockEntity;
import com.breakinblocks.horsepowered.blockentity.HPBlockEntityHorseBase;
import com.breakinblocks.horsepowered.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockGenerator extends BlockHPBase {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape SHAPE = Shapes.block();
    private static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public BlockGenerator(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public void emptiedOutput(Level level, BlockPos pos) {
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(be instanceof GeneratorBlockEntity gen)) return drops;
        if (gen.getEnergyHandler().getAmountAsInt() <= 0) return drops;

        TagValueOutput out = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, params.getLevel().registryAccess());
        gen.saveCustomOnly(out);
        CompoundTag beTag = out.buildResult();
        if (beTag.isEmpty()) return drops;

        TypedEntityData<BlockEntityType<?>> data = TypedEntityData.of(gen.getType(), beTag);
        for (ItemStack drop : drops) {
            if (drop.is(this.asItem())) {
                drop.set(DataComponents.BLOCK_ENTITY_DATA, data);
                break;
            }
        }
        return drops;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return checkType(type, ModBlockEntities.GENERATOR.get(), HPBlockEntityHorseBase::clientTick);
        } else {
            return checkType(type, ModBlockEntities.GENERATOR.get(), HPBlockEntityHorseBase::serverTick);
        }
    }
}
