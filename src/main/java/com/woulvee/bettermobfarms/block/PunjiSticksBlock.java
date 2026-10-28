package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.damage.BMFDamageTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// Bamboo spikes that poison their victims. They need a solid block below them.
public class PunjiSticksBlock extends SpikeBlock {
    public static final MapCodec<PunjiSticksBlock> CODEC = simpleCodec(PunjiSticksBlock::new);

    public PunjiSticksBlock(Properties properties) {
        super(1.5f, BMFDamageTypes.SPIKE_BAMBOO, properties);
    }

    @Override
    protected MapCodec<? extends PunjiSticksBlock> codec() {
        return CODEC;
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
    protected void onSpiked(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60));
    }
}
