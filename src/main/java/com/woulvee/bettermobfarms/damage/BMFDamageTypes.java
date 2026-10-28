package com.woulvee.bettermobfarms.damage;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

// Damage types are data driven, the JSON files live in data/bettermobfarms/damage_type
public class BMFDamageTypes {
    public static final ResourceKey<DamageType> SPIKE = key("spike");
    public static final ResourceKey<DamageType> SPIKE_BAMBOO = key("spike_bamboo");
    public static final ResourceKey<DamageType> MOB_KILLER = key("mob_killer");
    public static final ResourceKey<DamageType> BEAR_TRAP = key("bear_trap");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, name));
    }
}
