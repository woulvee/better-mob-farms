package com.woulvee.bettermobfarms;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.woulvee.bettermobfarms.block.BMFBlocks;
import com.woulvee.bettermobfarms.block.LyeCauldronBlock;
import com.woulvee.bettermobfarms.block.entity.BMFBlockEntities;
import com.woulvee.bettermobfarms.block.entity.ScarecrowBlockEntity;
import com.woulvee.bettermobfarms.effect.BMFEffects;
import com.woulvee.bettermobfarms.entity.ai.ScarecrowFleeGoal;
import com.woulvee.bettermobfarms.entity.ai.ScarecrowLureGoal;
import com.woulvee.bettermobfarms.item.BMFDataComponents;
import com.woulvee.bettermobfarms.item.BMFItems;
import com.woulvee.bettermobfarms.item.BMFAttachments;
import com.woulvee.bettermobfarms.item.PotionSyringeItem;
import com.woulvee.bettermobfarms.item.SoapBarItem;
import com.woulvee.bettermobfarms.particle.BMFParticles;
import com.woulvee.bettermobfarms.item.SyringeItem;
import com.woulvee.bettermobfarms.menu.BMFMenus;
import com.woulvee.bettermobfarms.recipe.BMFRecipes;
import com.woulvee.bettermobfarms.util.BMFTags;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCauldronInteractionEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BetterMobFarms.MODID)
public class BetterMobFarms {
    public static final String MODID = "bettermobfarms";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterMobFarms(IEventBus modEventBus, ModContainer modContainer) {
        BMFBlocks.BLOCKS.register(modEventBus);
        BMFItems.ITEMS.register(modEventBus);
        BMFItems.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(BMFItems::addToVanillaTabs);
        BMFBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        BMFDataComponents.DATA_COMPONENTS.register(modEventBus);
        BMFAttachments.ATTACHMENT_TYPES.register(modEventBus);
        BMFEffects.MOB_EFFECTS.register(modEventBus);
        BMFParticles.PARTICLE_TYPES.register(modEventBus);
        BMFMenus.MENUS.register(modEventBus);
        BMFRecipes.RECIPE_TYPES.register(modEventBus);
        BMFRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(RegisterCauldronInteractionEvent.Dispatcher.class, event ->
                event.register(Identifier.fromNamespaceAndPath(MODID, "lye"), LyeCauldronBlock.INTERACTIONS));
        modEventBus.addListener(RegisterCauldronInteractionEvent.Interaction.class, event ->
                event.register(Identifier.withDefaultNamespace("water"), Items.BONE_MEAL, LyeCauldronBlock::makeLye));

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // The syringe looks up extracting recipes on the client too, so they have to be synced
    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(BMFRecipes.EXTRACTING_TYPE.get());
    }

    // Goals are added here instead of with a mixin, so tags decide which mobs get them. Brain-only mobs don't
    // follow goals and belong in the scarecrow_lure_immune tag.
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }
        if (mob.is(BMFTags.Entities.SCARECROW_PESTS)) {
            mob.goalSelector.addGoal(1, new ScarecrowFleeGoal(mob));
        }
        if (mob instanceof Enemy && !(mob instanceof NeutralMob) && !mob.is(BMFTags.Entities.SCARECROW_LURE_IMMUNE)) {
            mob.goalSelector.addGoal(3, new ScarecrowLureGoal(mob));
        }
    }

    @SubscribeEvent
    public void onPlayerSpawnPhantoms(PlayerSpawnPhantomsEvent event) {
        Player player = event.getEntity();
        if (ScarecrowBlockEntity.findNearest(player.level(), player.position(), false, Config.SCARECROW_REPEL_RADIUS.getAsInt(), null) != null) {
            event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
        }
    }

    // Handled here instead of in Item#interactLivingEntity, because that only runs if the target ignores the click,
    // which villagers, horses and many other mobs don't
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        InteractionResult result = InteractionResult.PASS;
        if (stack.getItem() instanceof PotionSyringeItem syringe) {
            result = syringe.injectInto(stack, event.getEntity(), target);
        } else if (stack.getItem() instanceof SyringeItem syringe) {
            result = syringe.startSampling(event.getEntity(), event.getHand(), target);
        } else if (stack.getItem() instanceof SoapBarItem soapBar) {
            result = soapBar.soap(stack, event.getEntity(), event.getHand(), target);
        }
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
