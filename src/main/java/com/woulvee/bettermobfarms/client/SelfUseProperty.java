package com.woulvee.bettermobfarms.client;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

// Item model condition "bettermobfarms:self_use": true while the holder uses this syringe on itself,
// see SyringeClientExtensions#isSelfUsing
public record SelfUseProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<SelfUseProperty> MAP_CODEC = MapCodec.unit(new SelfUseProperty());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return owner != null && owner.getUseItem() == itemStack && SyringeClientExtensions.isSelfUsing(owner);
    }

    @Override
    public MapCodec<SelfUseProperty> type() {
        return MAP_CODEC;
    }
}
