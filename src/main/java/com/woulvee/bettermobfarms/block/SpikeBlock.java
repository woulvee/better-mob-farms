package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// A flat spike plate that hurts every living entity standing on it, unless it is sneaking
public class SpikeBlock extends Block {
    public static final MapCodec<SpikeBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.fieldOf("damage").forGetter(b -> b.damage),
            ResourceKey.codec(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(b -> b.damageType),
            propertiesCodec()
    ).apply(i, SpikeBlock::new));

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    private final float damage;
    private final ResourceKey<DamageType> damageType;

    public SpikeBlock(float damage, ResourceKey<DamageType> damageType, Properties properties) {
        super(properties);
        this.damage = damage;
        this.damageType = damageType;
    }

    @Override
    protected MapCodec<? extends SpikeBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living && !living.isSteppingCarefully()) {
            living.hurtServer(serverLevel, serverLevel.damageSources().source(damageType), damage);
            onSpiked(living);
        }
    }

    // Extra effects of a spike variant, called on the server after the damage was applied
    protected void onSpiked(LivingEntity entity) {
    }
}
