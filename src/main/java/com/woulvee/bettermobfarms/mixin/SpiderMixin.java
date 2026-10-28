package com.woulvee.bettermobfarms.mixin;

import com.woulvee.bettermobfarms.effect.BMFEffects;

import net.minecraft.world.entity.monster.spider.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Spiders override onClimbable to climb walls, so LivingEntityMixin alone does not stop them
@Mixin(Spider.class)
public abstract class SpiderMixin {
    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void bettermobfarms$noClimbingWhenSoaped(CallbackInfoReturnable<Boolean> cir) {
        if (((Spider) (Object) this).hasEffect(BMFEffects.SOAPED)) {
            cir.setReturnValue(false);
        }
    }
}
