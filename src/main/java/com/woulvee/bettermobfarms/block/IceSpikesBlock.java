package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.damage.BMFDamageTypes;

import net.minecraft.world.entity.LivingEntity;

public class IceSpikesBlock extends SpikeBlock {
    public static final MapCodec<IceSpikesBlock> CODEC = simpleCodec(IceSpikesBlock::new);

    private static final int FROST_PER_TICK = 4;
    private static final int FROST_AFTER_FROZEN = 40;

    public IceSpikesBlock(Properties properties) {
        super(2f, BMFDamageTypes.SPIKE, properties);
    }

    @Override
    protected MapCodec<? extends IceSpikesBlock> codec() {
        return CODEC;
    }

    // Frost builds up like in powder snow: entities thaw by 2 every tick, so standing on the spikes adds 2 net
    // and freezes the hearts after about 3.5 seconds. Capped a bit above fully frozen so they thaw about
    // a second after stepping off. Entities that can't freeze, like strays or players in leather, are spared.
    @Override
    protected void onSpiked(LivingEntity entity) {
        if (entity.canFreeze()) {
            int cap = entity.getTicksRequiredToFreeze() + FROST_AFTER_FROZEN;
            entity.setTicksFrozen(Math.min(cap, entity.getTicksFrozen() + FROST_PER_TICK));
        }
    }
}
