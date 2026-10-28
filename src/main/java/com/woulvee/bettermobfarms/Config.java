package com.woulvee.bettermobfarms;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_FAN_AIRFLOW = BUILDER
            .comment("Whether powered fans draw an outline around the area they blow through")
            .define("showFanAirflow", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BEAR_TRAP_BLACKLIST = BUILDER
            .comment("Mobs that walk over bear traps without setting them off, as entity ids like \"minecraft:iron_golem\"")
            .defineListAllowEmpty("bearTrapBlacklist", List.of("minecraft:ender_dragon", "minecraft:wither", "minecraft:iron_golem"),
                    () -> "minecraft:iron_golem", entry -> entry instanceof String id && Identifier.tryParse(id) != null);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean isBearTrapImmune(EntityType<?> type) {
        return BEAR_TRAP_BLACKLIST.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
    }
}
