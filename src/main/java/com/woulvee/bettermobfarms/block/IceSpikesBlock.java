package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.damage.BMFDamageTypes;

import net.minecraft.world.entity.LivingEntity;

public class IceSpikesBlock extends SpikeBlock {
    public static final MapCodec<IceSpikesBlock> CODEC = simpleCodec(IceSpikesBlock::new);

    public IceSpikesBlock(Properties properties) {
        super(2f, BMFDamageTypes.SPIKE, properties);
    }

    @Override
    protected MapCodec<? extends IceSpikesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onSpiked(LivingEntity entity) {
        entity.setTicksFrozen(60);
    }
}
