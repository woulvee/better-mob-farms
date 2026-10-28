package com.woulvee.bettermobfarms.client;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.platform.NativeImage;
import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

// The liquid color of DNA syringes per mob. In order of priority:
// 1. an override from the client config (ClientConfig#DNA_COLORS), for modpack makers
// 2. the most striking color of the mob's spawn egg texture, which also works for mobs from other mods
// 3. a color derived from the mob's id, for mobs without a spawn egg
// Colors are computed the first time they are needed and kept until resources or the config are reloaded.
public final class DnaColors {
    private static final int HUE_BINS = 12;
    // Below this brightness the liquid would look black in the dark syringe
    private static final float MIN_BRIGHTNESS = 0.55f;

    // Filled on the render thread, read by chunk building threads too (see IncubatorEggTint)
    private static final Map<EntityType<?>, Integer> CACHE = new ConcurrentHashMap<>();

    private DnaColors() {
    }

    // Render thread only: computing a color resolves item models
    public static int get(EntityType<?> type) {
        return CACHE.computeIfAbsent(type, DnaColors::compute);
    }

    // Safe on any thread, empty if the color was not computed yet
    public static OptionalInt getCached(EntityType<?> type) {
        Integer color = CACHE.get(type);
        return color != null ? OptionalInt.of(color) : OptionalInt.empty();
    }

    public static void clear() {
        CACHE.clear();
    }

    private static int compute(EntityType<?> type) {
        Integer override = ClientConfig.dnaColorOverride(type);
        if (override != null) {
            return override;
        }
        OptionalInt eggColor = spawnEggColor(type);
        return eggColor.isPresent() ? eggColor.getAsInt() : idColor(type);
    }

    // A stable color from the mob's id: the hue comes from its hash, saturation and brightness are fixed
    private static int idColor(EntityType<?> type) {
        int hash = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().hashCode();
        return Mth.hsvToRgb((hash & 0xFFFF) / 65536f, 0.65f, 0.9f);
    }

    private static OptionalInt spawnEggColor(EntityType<?> type) {
        return SpawnEggItem.byId(type).map(egg -> {
            try {
                // Resolve the egg's item model like the GUI does, so custom model setups of other mods work too
                ItemStackRenderState renderState = new ItemStackRenderState();
                Minecraft.getInstance().getItemModelResolver()
                        .updateForTopItem(renderState, new ItemStack(egg), ItemDisplayContext.GUI, null, null, 0);
                Material.Baked material = renderState.pickParticleMaterial(RandomSource.create(0));
                return material != null ? dominantColor(material.sprite().contents()) : OptionalInt.empty();
            } catch (RuntimeException e) {
                BetterMobFarms.LOGGER.debug("Could not read the spawn egg texture of {}", type, e);
                return OptionalInt.empty();
            }
        }).orElse(OptionalInt.empty());
    }

    // Groups the colorful pixels of the first frame by hue and averages the strongest group.
    // Eggs without real colors (black and white ones) use their average color instead.
    private static OptionalInt dominantColor(SpriteContents sprite) {
        NativeImage image = sprite.getOriginalImage();
        float[][] bins = new float[HUE_BINS][4];
        float grayR = 0, grayG = 0, grayB = 0;
        int opaque = 0;
        for (int y = 0; y < sprite.height(); y++) {
            for (int x = 0; x < sprite.width(); x++) {
                int argb = image.getPixel(x, y);
                if (ARGB.alpha(argb) < 128) {
                    continue;
                }
                float r = ARGB.red(argb) / 255f, g = ARGB.green(argb) / 255f, b = ARGB.blue(argb) / 255f;
                grayR += r;
                grayG += g;
                grayB += b;
                opaque++;
                float[] hsv = toHsv(r, g, b);
                float weight = hsv[1] * hsv[2];
                float[] bin = bins[Math.min(HUE_BINS - 1, (int) (hsv[0] * HUE_BINS))];
                bin[0] += r * weight;
                bin[1] += g * weight;
                bin[2] += b * weight;
                bin[3] += weight;
            }
        }
        if (opaque == 0) {
            return OptionalInt.empty();
        }

        float[] best = bins[0];
        for (float[] bin : bins) {
            if (bin[3] > best[3]) {
                best = bin;
            }
        }
        // Less than about three fully colorful pixels: treat the egg as gray
        float[] rgb = best[3] < 3
                ? new float[]{grayR / opaque, grayG / opaque, grayB / opaque}
                : new float[]{best[0] / best[3], best[1] / best[3], best[2] / best[3]};
        float[] hsv = toHsv(rgb[0], rgb[1], rgb[2]);
        return OptionalInt.of(Mth.hsvToRgb(hsv[0], hsv[1], Math.max(MIN_BRIGHTNESS, hsv[2])));
    }

    // Hue, saturation and value, all from 0 to 1
    private static float[] toHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float hue;
        if (delta == 0) {
            hue = 0;
        } else if (max == r) {
            hue = ((g - b) / delta + 6) % 6;
        } else if (max == g) {
            hue = (b - r) / delta + 2;
        } else {
            hue = (r - g) / delta + 4;
        }
        return new float[]{hue / 6, max == 0 ? 0 : delta / max, max};
    }
}
