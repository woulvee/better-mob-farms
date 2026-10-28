package com.woulvee.bettermobfarms.client;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.item.SyringeItem;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

// Item model condition "bettermobfarms:sampling": true while the holder takes a DNA sample with this syringe
public record IsSamplingProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<IsSamplingProperty> MAP_CODEC = MapCodec.unit(new IsSamplingProperty());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return owner != null && owner.isUsingItem() && owner.getUseItem() == itemStack && SyringeItem.isSampling(owner);
    }

    @Override
    public MapCodec<IsSamplingProperty> type() {
        return MAP_CODEC;
    }
}
