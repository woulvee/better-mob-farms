package com.woulvee.bettermobfarms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.woulvee.bettermobfarms.item.PotionSyringeItem;
import com.woulvee.bettermobfarms.item.SyringeItem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

// First person and third person animations of the empty syringe while it is used from the main hand:
// - Extracting: held against the off hand item, pumping in and out. The values are taken over from the old
//   Fabric ItemSyringeRenderer.
// - DNA sampling: a stab towards the crosshair. The syringe_sampling item model points the needle there.
// - Using it on yourself (sampling yourself, or injecting a potion syringe, which uses this class too):
//   the syringe_self item model points the needle at the camera. A quick stab towards it, then the syringe
//   slowly goes on in while injecting, or is slowly drawn out while sampling.
public class SyringeClientExtensions implements IClientItemExtensions {
    private static final float BOB_SCALE = 0.25f;
    // While extracting, the syringe and the off hand item (see SyringeOffhandRenderer) are both moved by this amount
    // towards the main hand side. That centers the off hand item and keeps the needle touching it.
    static final float COMPOSITION_SHIFT = 0.25f;
    // Not normalized on purpose, the old renderer used these exact quaternions
    private static final Quaternionfc RIGHT_HAND_POSE = new Quaternionf(0.1f, 0.4f, -0.6f, 1f);
    private static final Quaternionfc LEFT_HAND_POSE = new Quaternionf(-0.1f, -0.4f, 0.6f, 1f);
    // Needle direction of the syringe_sampling first person model in the right hand, and how far the stab reaches
    private static final Vector3fc STAB_DIRECTION = new Vector3f(-0.6f, 0.5f, -0.62f).normalize();
    private static final float STAB_REACH = 0.25f;
    // Needle direction of the syringe_self first person model in the right hand, towards the camera
    private static final Vector3fc SELF_STAB_DIRECTION = new Vector3f(-0.77f, 0.24f, 0.59f).normalize();
    // How far the quick stab moves the syringe, and how much further it goes in while injecting
    private static final float SELF_STAB_REACH = 0.2f;
    private static final float SELF_PUSH_REACH = 0.15f;
    private static final float SELF_STAB_TICKS = 3;

    private static boolean isUsingSyringe(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUsedItemHand() == InteractionHand.MAIN_HAND && entity.getUseItem().getItem() instanceof SyringeItem;
    }

    // True while the entity fills a syringe from its off hand
    static boolean isInjecting(LivingEntity entity) {
        return isUsingSyringe(entity) && !SyringeItem.isSampling(entity);
    }

    // True while the entity takes a DNA sample from a mob
    static boolean isSampling(LivingEntity entity) {
        return isUsingSyringe(entity) && SyringeItem.isSampling(entity);
    }

    // True while the entity samples itself or injects a potion syringe into itself, from either hand
    static boolean isSelfUsing(LivingEntity entity) {
        if (!entity.isUsingItem()) {
            return false;
        }
        Item item = entity.getUseItem().getItem();
        return item instanceof PotionSyringeItem || item instanceof SyringeItem && SyringeItem.isSelfSampling(entity);
    }

    // How far the syringe is pushed in, from 0 to 1: a quick stab, then it is slowly drawn back a bit while it fills
    static float stabProgress(float ticksUsing) {
        return ticksUsing < 3 ? Mth.sin(ticksUsing / 3 * Mth.HALF_PI) : 1 - 0.25f * Math.min(1, (ticksUsing - 3) / 14);
    }

    // How far the syringe is pushed in when used on yourself, from 0 to 1: a quick stab, then while injecting
    // it slowly goes on in, while drawing blood it is slowly pulled out again over the rest of the use time
    static float selfStabProgress(float ticksUsing, int useDuration, boolean injecting) {
        float stabbed = SELF_STAB_REACH / (SELF_STAB_REACH + SELF_PUSH_REACH);
        if (ticksUsing < SELF_STAB_TICKS) {
            return Mth.sin(ticksUsing / SELF_STAB_TICKS * Mth.HALF_PI) * stabbed;
        }
        float rest = Math.min(1, (ticksUsing - SELF_STAB_TICKS) / (useDuration - SELF_STAB_TICKS));
        return injecting ? Mth.lerp(rest, stabbed, 1) : Mth.lerp(rest, stabbed, 0);
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        boolean left = arm == HumanoidArm.LEFT;
        float side = left ? -1 : 1;
        float useTime = player.getTicksUsingItem();

        if (isSelfUsing(player) && player.getUseItem() == itemInHand) {
            boolean injecting = itemInHand.getItem() instanceof PotionSyringeItem;
            float progress = selfStabProgress(useTime + partialTick, itemInHand.getUseDuration(player), injecting);
            float reach = progress * (SELF_STAB_REACH + SELF_PUSH_REACH);
            poseStack.translate(side * 0.56f, -0.52f + equipProcess * -0.6f, -0.72f);
            poseStack.translate(side * SELF_STAB_DIRECTION.x() * reach, SELF_STAB_DIRECTION.y() * reach, SELF_STAB_DIRECTION.z() * reach);
            return true;
        }
        if (!isUsingSyringe(player) || player.getUseItem() != itemInHand) {
            return false;
        }

        if (SyringeItem.isSampling(player)) {
            float stab = stabProgress(useTime + partialTick) * STAB_REACH;
            poseStack.translate(side * 0.56f, -0.52f + equipProcess * -0.6f, -0.72f);
            poseStack.translate(side * STAB_DIRECTION.x() * stab, STAB_DIRECTION.y() * stab, STAB_DIRECTION.z() * stab);
            return true;
        }

        // Vanilla's arm transform for held items, moved along with the off hand item
        poseStack.translate(side * (0.56f + COMPOSITION_SHIFT), -0.52f + equipProcess * -0.6f, -0.72f);

        float bob = Mth.lerp(partialTick, bob(useTime), bob(useTime + 1));

        // The old renderer worked in block space, with the origin in the corner of the item
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        poseStack.translate(left ? 1.28f - bob : -0.28f + bob, 0.73f, 0.55f);
        poseStack.scale(0.7f, 0.7f, 0.7f);
        poseStack.mulPose(left ? LEFT_HAND_POSE : RIGHT_HAND_POSE);
        return true;
    }

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        if (isSelfUsing(entityLiving)) {
            return hand == entityLiving.getUsedItemHand() ? SyringeArmPose.SELF.getValue() : null;
        }
        if (hand != InteractionHand.MAIN_HAND || !isUsingSyringe(entityLiving)) {
            return null;
        }
        return (SyringeItem.isSampling(entityLiving) ? SyringeArmPose.SAMPLING : SyringeArmPose.POSE).getValue();
    }

    private static float bob(float useTime) {
        return -Math.abs(Mth.cos(useTime) * BOB_SCALE);
    }
}
