package com.woulvee.bettermobfarms.effect;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, BetterMobFarms.MODID);

    // Held by a bear trap: no walking and no jumping, attacking still works
    public static final DeferredHolder<MobEffect, MobEffect> TRAPPED = MOB_EFFECTS.register("trapped", () -> new TrappedEffect()
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, id("trapped_movement"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.JUMP_STRENGTH, id("trapped_jump"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> SOAPED = MOB_EFFECTS.register("soaped", SoapedEffect::new);

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, name);
    }

    // MobEffect's constructor is protected
    private static class TrappedEffect extends MobEffect {
        TrappedEffect() {
            super(MobEffectCategory.HARMFUL, 0x5A5A5A);
        }
    }
}
