package com.woulvee.bettermobfarms.particle;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, BetterMobFarms.MODID);

    // Left on the ground by soaped entities, see client.SoapBubbleParticle
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOAP_BUBBLE = PARTICLE_TYPES.register("soap_bubble",
            () -> new SimpleParticleType(false));
}
