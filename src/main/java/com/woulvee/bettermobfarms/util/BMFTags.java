package com.woulvee.bettermobfarms.util;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class BMFTags {
    public static class Entities {
        // Mobs a syringe can not take DNA from
        public static final TagKey<EntityType<?>> SYRINGE_DNA_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "syringe_dna_blacklist"));
        // Animals a feeding trough lures and feeds
        public static final TagKey<EntityType<?>> LIVESTOCK = TagKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "livestock"));
        // Mobs that walk over bear traps without setting them off
        public static final TagKey<EntityType<?>> BEAR_TRAP_IMMUNE = TagKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "bear_trap_immune"));
        // Animals that run from scarecrows without a player head
        public static final TagKey<EntityType<?>> SCARECROW_PESTS = TagKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "scarecrow_pests"));
        // Hostile mobs that scarecrows with a player head do not lure
        public static final TagKey<EntityType<?>> SCARECROW_LURE_IMMUNE = TagKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "scarecrow_lure_immune"));
    }
}
