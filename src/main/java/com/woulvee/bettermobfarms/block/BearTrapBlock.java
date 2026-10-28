package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.Config;
import com.woulvee.bettermobfarms.damage.BMFDamageTypes;
import com.woulvee.bettermobfarms.effect.BMFEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Snaps shut on the first living entity that steps into it: 2.5 hearts of damage and 3 seconds without moving.
// A closed trap does nothing until a player opens it again with a right click.
public class BearTrapBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BearTrapBlock> CODEC = simpleCodec(BearTrapBlock::new);
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    private static final float DAMAGE = 5f;
    private static final int HOLD_TICKS = 60;
    private static final VoxelShape OPEN_SHAPE = Block.box(1, 0, 2, 15, 3, 14);
    private static final VoxelShape CLOSED_SHAPE = Block.box(1, 0, 2, 15, 6, 14);

    public BearTrapBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(CLOSED, false));
    }

    @Override
    protected MapCodec<? extends BearTrapBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CLOSED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(CLOSED) ? CLOSED_SHAPE : OPEN_SHAPE;
    }

    // Entities walk into the trap instead of on top of it
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportRigidBlock(level, pos.below());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (state.getValue(CLOSED) || !(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity living)
                || living.isSpectator() || Config.isBearTrapImmune(living.getType())) {
            return;
        }
        level.setBlockAndUpdate(pos, state.setValue(CLOSED, true));
        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1f, 0.6f);

        // Pull the victim into the middle of the trap so it visibly sits in the jaws
        living.teleportTo(pos.getX() + 0.5, living.getY(), pos.getZ() + 0.5);
        living.setDeltaMovement(0, Math.min(0, living.getDeltaMovement().y), 0);
        living.hurtServer(serverLevel, serverLevel.damageSources().source(BMFDamageTypes.BEAR_TRAP), DAMAGE);
        living.addEffect(new MobEffectInstance(BMFEffects.TRAPPED, HOLD_TICKS, 0, false, false, true));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!state.getValue(CLOSED)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, state.setValue(CLOSED, false));
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1f, 0.8f);
        }
        return InteractionResult.SUCCESS;
    }
}
