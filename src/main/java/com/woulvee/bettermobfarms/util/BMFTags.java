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
    }
}
