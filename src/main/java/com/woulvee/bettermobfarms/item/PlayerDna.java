package com.woulvee.bettermobfarms.item;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

// The player a DNA sample was taken from. The name is the one the player had when sampled, the id stays the same.
public record PlayerDna(UUID id, String name) {
    public static final Codec<PlayerDna> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(PlayerDna::id),
            Codec.STRING.fieldOf("name").forGetter(PlayerDna::name)
    ).apply(instance, PlayerDna::new));

    public static final StreamCodec<ByteBuf, PlayerDna> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerDna::id,
            ByteBufCodecs.STRING_UTF8, PlayerDna::name,
            PlayerDna::new);

    public static PlayerDna of(Player player) {
        return new PlayerDna(player.getUUID(), player.getGameProfile().name());
    }
}
