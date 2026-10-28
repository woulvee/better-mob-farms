package com.woulvee.bettermobfarms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

// While injecting, the item in the off hand is drawn in the middle of the screen.
// SyringeClientExtensions moves the syringe by the same amount, so the needle still touches the item.
@EventBusSubscriber(modid = BetterMobFarms.MODID, value = Dist.CLIENT)
public class SyringeOffhandRenderer {

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack stack = event.getItemStack();
        if (event.getHand() != InteractionHand.OFF_HAND || player == null || stack.isEmpty() || !SyringeClientExtensions.isInjecting(player)) {
            return;
        }
        event.setCanceled(true);

        boolean left = player.getMainArm().getOpposite() == HumanoidArm.LEFT;
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        // Vanilla's arm transform for held items, moved along with the syringe
        float x = 0.56f - SyringeClientExtensions.COMPOSITION_SHIFT;
        poseStack.translate((left ? -1 : 1) * x, -0.52f + event.getEquipProgress() * -0.6f, -0.72f);
        Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player, stack,
                left ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                poseStack, event.getSubmitNodeCollector(), event.getPackedLight());
        poseStack.popPose();
    }
}
