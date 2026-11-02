package com.woulvee.bettermobfarms.item;

import java.util.Optional;

import com.woulvee.bettermobfarms.recipe.ExtractingRecipe;
import com.woulvee.bettermobfarms.util.BMFTags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

// The empty syringe has two uses:
// - Hold right click with an extractable item in the off hand to fill the syringe.
// - Hold right click on a mob or another player for a second to take a DNA sample. The prick hurts the target
//   right away, so hostile and neutral mobs fight back. Sneaking and holding right click samples yourself.
public class SyringeItem extends Item {
    private static final int EXTRACT_DURATION = 38;
    private static final int SAMPLE_DURATION = 20;
    private static final float SAMPLE_DAMAGE = 2f;

    public SyringeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        stopSampling(player);
        // Filling from the off hand wins over sampling yourself
        if (findRecipe(level, player).isPresent()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (player.isSecondaryUseActive()) {
            return sample(player, hand, player);
        }
        return InteractionResult.PASS;
    }

    // Called from BetterMobFarms#onEntityInteract on both sides, before the target can handle the click itself.
    // Sneaking passes, so the click goes on to use() and samples the player instead.
    public InteractionResult startSampling(Player player, InteractionHand hand, LivingEntity target) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        return sample(player, hand, target);
    }

    private InteractionResult sample(Player player, InteractionHand hand, LivingEntity target) {
        if (target.is(BMFTags.Entities.SYRINGE_DNA_BLACKLIST) || player.isUsingItem()) {
            return InteractionResult.PASS;
        }
        // The target has to be known before using starts, it decides the use duration
        player.setData(BMFAttachments.SAMPLING_TARGET, target.getId());
        player.startUsingItem(hand);
        if (player.level() instanceof ServerLevel level) {
            DamageSource source = target == player ? level.damageSources().generic() : level.damageSources().playerAttack(player);
            target.hurtServer(level, source, SAMPLE_DAMAGE);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        boolean canContinue = isSampling(livingEntity)
                ? samplingTarget(level, livingEntity) != null
                : findRecipe(level, livingEntity).isPresent();
        if (!canContinue) {
            stopSampling(livingEntity);
            livingEntity.stopUsingItem();
            return;
        }
        int usedTicks = getUseDuration(itemStack, livingEntity) - ticksRemaining + 1;
        if (usedTicks == 1 || usedTicks % 5 == 0 && usedTicks < getUseDuration(itemStack, livingEntity) - 6) {
            playSound(level, livingEntity, SoundEvents.GENERIC_DRINK.value(), 1f);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            if (isSampling(player)) {
                LivingEntity target = samplingTarget(level, player);
                if (target != null) {
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    ItemStack dnaSyringe = BMFItems.dnaSyringe(target.getType());
                    if (target instanceof Player sampled) {
                        dnaSyringe.set(BMFDataComponents.PLAYER_DNA.get(), PlayerDna.of(sampled));
                    }
                    player.getInventory().placeItemBackInInventory(dnaSyringe);
                }
            } else {
                findRecipe(level, player).ifPresent(recipe -> {
                    ItemStack input = player.getOffhandItem();
                    ItemStack filledSyringe = recipe.assemble(new SingleRecipeInput(input));
                    ItemStack byproduct = recipe.byproduct();
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                        input.shrink(1);
                    }
                    player.getInventory().placeItemBackInInventory(filledSyringe);
                    if (!byproduct.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(byproduct);
                    }
                });
            }
        }
        stopSampling(entity);
        playSound(level, entity, SoundEvents.WITCH_DRINK, 0.8f);
        return itemStack;
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        stopSampling(entity);
        return false;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return isSampling(user) ? SAMPLE_DURATION : EXTRACT_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.NONE;
    }

    // Like swords, the syringe can not break blocks in creative mode
    @Override
    public boolean canDestroyBlock(ItemStack itemStack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return !(user instanceof Player player && player.isCreative());
    }

    public static boolean isSampling(LivingEntity user) {
        return user.getData(BMFAttachments.SAMPLING_TARGET) >= 0;
    }

    public static boolean isSelfSampling(LivingEntity user) {
        return user.getData(BMFAttachments.SAMPLING_TARGET) == user.getId();
    }

    private static void stopSampling(LivingEntity user) {
        user.setData(BMFAttachments.SAMPLING_TARGET, -1);
    }

    // The mob being sampled, or null if it died or the player moved out of reach
    private static @Nullable LivingEntity samplingTarget(Level level, LivingEntity user) {
        Entity target = level.getEntity(user.getData(BMFAttachments.SAMPLING_TARGET));
        if (target instanceof LivingEntity living && living.isAlive() && user instanceof Player player && player.isWithinEntityInteractionRange(living, 1.0)) {
            return living;
        }
        return null;
    }

    private static Optional<ExtractingRecipe> findRecipe(Level level, LivingEntity user) {
        return ExtractingRecipe.find(level, user.getOffhandItem());
    }

    private static void playSound(Level level, LivingEntity user, SoundEvent sound, float pitch) {
        Player except = user instanceof Player player ? player : null;
        level.playSound(except, user.blockPosition(), sound, SoundSource.PLAYERS, 1f, pitch);
    }
}
