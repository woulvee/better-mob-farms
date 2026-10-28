package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.damage.BMFDamageTypes;

import net.minecraft.world.entity.LivingEntity;

public class HotNetheriteSpikesBlock extends SpikeBlock {
    public static final MapCodec<HotNetheriteSpikesBlock> CODEC = simpleCodec(HotNetheriteSpikesBlock::new);

    public HotNetheriteSpikesBlock(Properties properties) {
        super(6f, BMFDamageTypes.SPIKE, properties);
    }

    @Override
    protected MapCodec<? extends HotNetheriteSpikesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onSpiked(LivingEntity entity) {
        entity.igniteForSeconds(1);
    }
}
