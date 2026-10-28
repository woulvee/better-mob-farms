package com.woulvee.bettermobfarms.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.woulvee.bettermobfarms.item.BMFDataComponents;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

// Item model tint "bettermobfarms:dna": the DNA color of the mob stored in the stack, see DnaColors.
// "default" is used for syringes without DNA.
public record DnaTintSource(int defaultColor) implements ItemTintSource {
    public static final MapCodec<DnaTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(DnaTintSource::defaultColor)).apply(i, DnaTintSource::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        EntityType<?> dna = itemStack.get(BMFDataComponents.DNA.get());
        return ARGB.opaque(dna != null ? DnaColors.get(dna) : defaultColor);
    }

    @Override
    public MapCodec<DnaTintSource> type() {
        return MAP_CODEC;
    }
}
