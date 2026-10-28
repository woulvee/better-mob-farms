package com.woulvee.bettermobfarms.client;

import java.util.List;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jspecify.annotations.Nullable;

// bettermobfarms-client.toml. Colors only matter for rendering, so this is a client config.
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> DNA_COLORS = BUILDER
            .comment("Overrides the liquid color of DNA syringes for single mobs.",
                    "Format: \"<entity id>=#RRGGBB\", for example \"minecraft:zombie=#3E8E41\".",
                    "Mobs without an entry use the most striking color of their spawn egg,",
                    "or a color derived from their id if they have no spawn egg.")
            .defineListAllowEmpty("dnaColors", List.of(), () -> "minecraft:zombie=#3E8E41", ClientConfig::isValidColorEntry);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean isValidColorEntry(Object entry) {
        if (!(entry instanceof String text)) {
            return false;
        }
        String[] parts = text.split("=", 2);
        return parts.length == 2 && Identifier.tryParse(parts[0].trim()) != null && parseColor(parts[1]) != null;
    }

    // The configured color for a mob, or null if it has none
    static @Nullable Integer dnaColorOverride(EntityType<?> type) {
        if (!SPEC.isLoaded()) {
            return null;
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        for (String entry : DNA_COLORS.get()) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals(id)) {
                return parseColor(parts[1]);
            }
        }
        return null;
    }

    private static @Nullable Integer parseColor(String text) {
        String hex = text.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6) {
            return null;
        }
        try {
            return 0xFF000000 | Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            BetterMobFarms.LOGGER.debug("Invalid DNA color {}", text);
            return null;
        }
    }
}
