package com.woulvee.bettermobfarms;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_FAN_AIRFLOW = BUILDER
            .comment("Whether powered fans draw an outline around the area they blow through")
            .define("showFanAirflow", false);

    public static final ModConfigSpec.IntValue FEEDING_TROUGH_INTERVAL = BUILDER
            .comment("Seconds between two feedings of a feeding trough")
            .defineInRange("feedingTroughInterval", 10, 1, 600);

    public static final ModConfigSpec.IntValue FEEDING_TROUGH_RANGE = BUILDER
            .comment("How many blocks away feeding troughs lure livestock from")
            .defineInRange("feedingTroughRange", 12, 1, 16);

    public static final ModConfigSpec.IntValue FEEDING_TROUGH_MAX_ANIMALS = BUILDER
            .comment("Feeding troughs stop feeding a kind of animal once this many of it are within their lure range")
            .defineInRange("feedingTroughMaxAnimals", 24, 2, 256);

    public static final ModConfigSpec.IntValue SCARECROW_REPEL_RADIUS = BUILDER
            .comment("How many blocks around a scarecrow without a player head pests and phantoms are kept away from")
            .defineInRange("scarecrowRepelRadius", 12, 1, 64);

    public static final ModConfigSpec.IntValue SCARECROW_LURE_RADIUS = BUILDER
            .comment("How many blocks away a scarecrow with a player head lures hostile mobs from")
            .defineInRange("scarecrowLureRadius", 16, 1, 32);

    public static final ModConfigSpec.IntValue SCARECROW_IGNORE_TIME = BUILDER
            .comment("Seconds a hostile mob ignores a scarecrow after hitting it")
            .defineInRange("scarecrowIgnoreTime", 60, 1, 3600);

    static final ModConfigSpec SPEC = BUILDER.build();
}
