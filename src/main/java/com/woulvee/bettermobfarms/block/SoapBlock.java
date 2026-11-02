package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.effect.BMFEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

// A thin layer of soap on the floor or on a wall, applied with a soap bar.
// Everything that touches it gets soaped for 3 seconds, which also stops spiders from climbing up the soaped wall.
public class SoapBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final MapCodec<SoapBlock> CODEC = simpleCodec(SoapBlock::new);
    public static final int SOAPED_TICKS = 60;

    private static final VoxelShape FLOOR_SHAPE = Block.box(0, 0, 0, 16, 1, 16);
    // FACING points away from the wall, the layer sits on the opposite side of the block
    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape EAST_SHAPE = Block.box(0, 0, 0, 1, 16, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(15, 0, 0, 16, 16, 16);

    public SoapBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACE, AttachFace.FLOOR).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends SoapBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING);
    }

    // The soap state for the clicked face of a block, null for ceilings
    public @Nullable BlockState stateFor(Direction clickedFace) {
        if (clickedFace == Direction.UP) {
            return defaultBlockState();
        }
        return clickedFace == Direction.DOWN ? null : defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, clickedFace);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(FACE) != AttachFace.CEILING && super.canSurvive(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(FACE) != AttachFace.WALL) {
            return FLOOR_SHAPE;
        }
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    // Neighboring soap layers on the same floor or wall hide the thin edges between them, so they look like one layer.
    // Only the edge faces of the models have a cullface, the flat sides are never skipped.
    @Override
    protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
        return neighborState.is(this) && neighborState.getValue(FACE) == state.getValue(FACE)
                && neighborState.getValue(FACING) == state.getValue(FACING) && isEdge(state, direction);
    }

    private static boolean isEdge(BlockState state, Direction direction) {
        if (state.getValue(FACE) != AttachFace.WALL) {
            return direction.getAxis().isHorizontal();
        }
        return direction.getAxis() != state.getValue(FACING).getAxis();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    // Uses the thin shape, so only entities actually touching the soap are affected
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && !living.isSpectator()) {
            MobEffectInstance current = living.getEffect(BMFEffects.SOAPED);
            if (current == null || current.getDuration() < SOAPED_TICKS) {
                living.addEffect(new MobEffectInstance(BMFEffects.SOAPED, SOAPED_TICKS, 0, false, false, true));
            }
        }
    }
}
