package com.woulvee.bettermobfarms.item;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

// A syringe filled with a potion. Right clicking a player or mob injects it, which applies the potion's effects
// to the target. Sneaking and holding right click injects it into yourself, in half the time of drinking a potion.
// The liquid takes the potion's color through the minecraft:potion tint in its item model.
public class PotionSyringeItem extends Item {
    private static final int SELF_INJECT_DURATION = 16;

    public PotionSyringeItem(Properties properties) {
        super(properties);
    }

    // Called from BetterMobFarms#onEntityInteract on both sides, before the target can handle the click itself.
    // Sneaking passes, so the click goes on to use() and injects the player instead.
    public InteractionResult injectInto(ItemStack itemStack, Player player, LivingEntity target) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (player.level() instanceof ServerLevel level) {
            inject(level, itemStack, player, target);
            if (!player.isCreative()) {
                itemStack.shrink(1);
                player.getInventory().placeItemBackInInventory(new ItemStack(BMFItems.SYRINGE.get()));
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void inject(ServerLevel level, ItemStack itemStack, Player player, LivingEntity target) {
        PotionContents potion = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        potion.forEachEffect(effect -> {
            if (effect.getEffect().value().isInstantenous()) {
                effect.getEffect().value().applyInstantenousEffect(level, player, player, target, effect.getAmplifier(), 1.0);
            } else {
                target.addEffect(effect, player);
            }
        }, 1f);
        level.playSound(null, target.blockPosition(), SoundEvents.WITCH_DRINK, SoundSource.PLAYERS, 1f, 0.8f);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            inject(serverLevel, itemStack, player, player);
            return ItemUtils.createFilledResult(itemStack, player, new ItemStack(BMFItems.SYRINGE.get()));
        }
        return itemStack;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return SELF_INJECT_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.NONE;
    }

    // "Syringe with Poison". Like vanilla potions, every potion name has its own translation key.
    // Potions from other mods have none, they fall back to the plain item name.
    @Override
    public Component getName(ItemStack itemStack) {
        PotionContents potion = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potion == null) {
            return super.getName(itemStack);
        }
        String potionName = potion.customName().or(() -> potion.potion().map(p -> p.value().name())).orElse("empty");
        return Component.translatableWithFallback("item." + BetterMobFarms.MODID + ".potion_syringe.effect." + potionName, "Potion Syringe");
    }
}
