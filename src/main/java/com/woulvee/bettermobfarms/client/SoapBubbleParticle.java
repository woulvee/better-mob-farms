package com.woulvee.bettermobfarms.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

// A bubble that sits on the ground for a few seconds, drifts a little and fades out
public class SoapBubbleParticle extends SingleQuadParticle {
    private static final int FADE_TICKS = 10;

    public SoapBubbleParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0, 0, 0, sprite);
        this.xd = (random.nextFloat() - 0.5f) * 0.01f;
        this.yd = 0;
        this.zd = (random.nextFloat() - 0.5f) * 0.01f;
        this.gravity = 0;
        this.quadSize *= 0.5f + random.nextFloat() * 0.5f;
        this.lifetime = 40 + random.nextInt(40);
    }

    @Override
    public void tick() {
        super.tick();
        int remaining = lifetime - age;
        if (remaining < FADE_TICKS) {
            setAlpha((float) remaining / FADE_TICKS);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new SoapBubbleParticle(level, x, y, z, sprite.get(random));
        }
    }
}
