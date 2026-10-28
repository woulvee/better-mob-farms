package com.woulvee.bettermobfarms.effect;

import com.woulvee.bettermobfarms.particle.BMFParticles;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

// Soaped entities slide like on ice and can't climb, see mixin.LivingEntityMixin and mixin.SpiderMixin.
// While they move on the ground they leave soap bubbles behind.
public class SoapedEffect extends MobEffect {
    // The friction of ice, used instead of the block below
    public static final float FRICTION = 0.98f;
    // Squared distance per tick an entity has to move to leave bubbles
    private static final double MIN_MOVEMENT_SQR = 0.03 * 0.03;

    public SoapedEffect() {
        super(MobEffectCategory.HARMFUL, 0xE6F4FF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % 3 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        // Players move on their client, getKnownSpeed is the movement the server last heard of
        if (mob.onGround() && mob.getKnownSpeed().horizontalDistanceSqr() > MIN_MOVEMENT_SQR) {
            level.sendParticles(BMFParticles.SOAP_BUBBLE.get(), mob.getX(), mob.getY() + 0.05, mob.getZ(),
                    1, mob.getBbWidth() * 0.3, 0, mob.getBbWidth() * 0.3, 0);
        }
        return true;
    }
}
