package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// The egg shown inside the incubator, each value has its own block model.
// EGG, BLUE_EGG and BROWN_EGG also tell which egg a mutated egg came from (BMFDataComponents#ORIGIN_EGG).
public enum IncubatorEgg implements StringRepresentable {
    NONE("none"),
    EGG("egg"),
    BLUE_EGG("blue_egg"),
    BROWN_EGG("brown_egg"),
    // Colored with the DNA of the egg by the incubator's block tint
    MUTATED_EGG("mutated_egg"),
    // The generic larger egg shown once a spawn egg is ready
    SPAWN_EGG("spawn_egg");

    public static final Codec<IncubatorEgg> CODEC = StringRepresentable.fromEnum(IncubatorEgg::values);
    public static final StreamCodec<ByteBuf, IncubatorEgg> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], IncubatorEgg::ordinal);

    private final String name;

    IncubatorEgg(String name) {
        this.name = name;
    }

    // The model for a vanilla egg item, other eggs of the minecraft:eggs tag look like a chicken egg
    public static IncubatorEgg of(ItemStack egg) {
        if (egg.isEmpty()) {
            return NONE;
        }
        if (egg.is(Items.BLUE_EGG)) {
            return BLUE_EGG;
        }
        return egg.is(Items.BROWN_EGG) ? BROWN_EGG : EGG;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
