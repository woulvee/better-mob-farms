package com.woulvee.bettermobfarms.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

// Third person poses of the empty syringe. The enum values are added through META-INF/enumextensions.json.
public class SyringeArmPose {
    // Extracting: both arms are raised in front of the chest, the syringe arm points at the off hand
    // and pumps in and out like the first person animation
    public static final EnumProxy<HumanoidModel.ArmPose> POSE = new EnumProxy<>(HumanoidModel.ArmPose.class,
            true, true, (IArmPoseTransformer) SyringeArmPose::applyExtracting);
    // DNA sampling: the syringe arm stabs forward where the player looks, the other arm is left alone
    public static final EnumProxy<HumanoidModel.ArmPose> SAMPLING = new EnumProxy<>(HumanoidModel.ArmPose.class,
            false, false, (IArmPoseTransformer) SyringeArmPose::applySampling);

    private static final float BOB_SCALE = 0.25f;
    // How far the arm moves forward for the stab, in model pixels
    private static final float STAB_REACH = 2.5f;

    private static void applyExtracting(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        boolean right = arm == HumanoidArm.RIGHT;
        ModelPart syringeArm = right ? model.rightArm : model.leftArm;
        ModelPart holdingArm = right ? model.leftArm : model.rightArm;
        // Positive yaw turns the left arm inwards, negative yaw the right one
        float inwards = right ? -1 : 1;

        float bob = Math.abs(Mth.cos(state.ticksUsingItem)) * BOB_SCALE;
        float headPitch = model.head.xRot * 0.5f;

        syringeArm.xRot = -1.25f + headPitch;
        syringeArm.yRot = inwards * (0.65f - bob);
        // The holding arm is rolled so the item faces the syringe, whose needle points down into it
        // (see the syringe_injecting item model)
        holdingArm.xRot = -0.95f + headPitch;
        holdingArm.yRot = -inwards * 0.4f;
        holdingArm.zRot = inwards * 0.5f;
    }

    // The syringe_sampling item model points the needle forward along this arm
    private static void applySampling(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        boolean right = arm == HumanoidArm.RIGHT;
        ModelPart syringeArm = right ? model.rightArm : model.leftArm;
        float inwards = right ? -1 : 1;

        syringeArm.xRot = -1.45f + model.head.xRot;
        syringeArm.yRot = model.head.yRot + inwards * 0.1f;
        syringeArm.z -= SyringeClientExtensions.stabProgress(state.ticksUsingItem) * STAB_REACH;
    }
}
