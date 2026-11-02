package com.woulvee.bettermobfarms;

import java.util.List;

import com.woulvee.bettermobfarms.block.BMFBlocks;
import com.woulvee.bettermobfarms.block.entity.BMFBlockEntities;
import com.woulvee.bettermobfarms.client.IncubatorEggTint;
import com.woulvee.bettermobfarms.client.SoapBubbleParticle;
import com.woulvee.bettermobfarms.effect.BMFEffects;
import com.woulvee.bettermobfarms.particle.BMFParticles;
import com.woulvee.bettermobfarms.client.ClientConfig;
import com.woulvee.bettermobfarms.client.DnaColors;
import com.woulvee.bettermobfarms.client.DnaTintSource;
import com.woulvee.bettermobfarms.client.FanRenderer;
import com.woulvee.bettermobfarms.client.FanScreen;
import com.woulvee.bettermobfarms.client.FeedingTroughScreen;
import com.woulvee.bettermobfarms.client.IncubatorScreen;
import com.woulvee.bettermobfarms.client.IsSamplingProperty;
import com.woulvee.bettermobfarms.client.ScarecrowRenderer;
import com.woulvee.bettermobfarms.client.SelfUseProperty;
import com.woulvee.bettermobfarms.client.SyringeClientExtensions;
import com.woulvee.bettermobfarms.item.BMFItems;
import com.woulvee.bettermobfarms.menu.BMFMenus;
import com.woulvee.bettermobfarms.recipe.BMFRecipes;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = BetterMobFarms.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BetterMobFarms.MODID, value = Dist.CLIENT)
public class BetterMobFarmsClient {
    // How much of the usual FOV change remains while a bear trap holds the player
    private static final float TRAPPED_FOV_SCALE = 0.15f;

    public BetterMobFarmsClient(ModContainer container) {
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "dna"), DnaTintSource.MAP_CODEC);
    }

    private static final int LYE_COLOR = 0xFFD9D4B4;

    @SubscribeEvent
    static void onRegisterBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new IncubatorEggTint()), BMFBlocks.INCUBATOR.get());
        // Lye is a milky, pale yellow instead of the biome water color
        event.register(List.of(state -> LYE_COLOR), BMFBlocks.LYE_CAULDRON.get());
    }

    // DNA colors come from spawn egg textures and the client config, so they are recomputed when either changes
    @SubscribeEvent
    static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "dna_colors"),
                (ResourceManagerReloadListener) resourceManager -> DnaColors.clear());
    }

    @SubscribeEvent
    static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ClientConfig.SPEC) {
            DnaColors.clear();
            // Mutated eggs in incubators are part of the chunk mesh, rebuild it with the new colors
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> {
                if (minecraft.level != null) {
                    minecraft.levelRenderer.allChanged();
                }
            });
        }
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BMFBlockEntities.FAN.get(), FanRenderer::new);
        event.registerBlockEntityRenderer(BMFBlockEntities.SCARECROW.get(), ScarecrowRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new SyringeClientExtensions(), BMFItems.SYRINGE.get(), BMFItems.POTION_SYRINGE.get());
    }

    @SubscribeEvent
    static void onRegisterItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "sampling"), IsSamplingProperty.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "self_use"), SelfUseProperty.MAP_CODEC);
    }

    @SubscribeEvent
    static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BMFParticles.SOAP_BUBBLE.get(), SoapBubbleParticle.Provider::new);
    }

    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(BMFMenus.FAN.get(), FanScreen::new);
        event.register(BMFMenus.INCUBATOR.get(), IncubatorScreen::new);
        event.register(BMFMenus.FEEDING_TROUGH.get(), FeedingTroughScreen::new);
    }

    // The trapped effect zeroes the movement speed, which vanilla turns into a strong zoom. Keep only a hint of it.
    @SubscribeEvent
    static void onComputeFovModifier(ComputeFovModifierEvent event) {
        if (event.getPlayer().hasEffect(BMFEffects.TRAPPED)) {
            event.setNewFovModifier(1 + (event.getNewFovModifier() - 1) * TRAPPED_FOV_SCALE);
        }
    }

    @SubscribeEvent
    static void onRecipesReceived(RecipesReceivedEvent event) {
        BMFRecipes.setClientRecipes(event.getRecipeMap());
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        BMFRecipes.setClientRecipes(RecipeMap.EMPTY);
    }
}
