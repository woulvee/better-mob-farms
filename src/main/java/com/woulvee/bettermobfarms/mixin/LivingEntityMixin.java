package com.woulvee.bettermobfarms.mixin;

import com.woulvee.bettermobfarms.effect.BMFEffects;
import com.woulvee.bettermobfarms.effect.SoapedEffect;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The soaped effect: slide on any ground like on ice, and no climbing ladders or vines
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // blockFriction is the first float local of travelInAir, read from the block below
    @ModifyVariable(method = "travelInAir", at = @At("STORE"), ordinal = 0)
    private float bettermobfarms$soapedFriction(float blockFriction) {
        LivingEntity self = (LivingEntity) (Object) this;
        return self.onGround() && self.hasEffect(BMFEffects.SOAPED) ? Math.max(blockFriction, SoapedEffect.FRICTION) : blockFriction;
    }

    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void bettermobfarms$noClimbingWhenSoaped(CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this).hasEffect(BMFEffects.SOAPED)) {
            cir.setReturnValue(false);
        }
    }
}
