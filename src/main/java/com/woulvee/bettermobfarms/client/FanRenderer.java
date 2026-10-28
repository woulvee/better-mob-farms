package com.woulvee.bettermobfarms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.woulvee.bettermobfarms.Config;
import com.woulvee.bettermobfarms.block.BMFBlocks;
import com.woulvee.bettermobfarms.block.FanBlock;
import com.woulvee.bettermobfarms.block.FanPropellerBlock;
import com.woulvee.bettermobfarms.block.entity.FanBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Spins the propeller of powered fans and outlines their airflow
public class FanRenderer implements BlockEntityRenderer<FanBlockEntity, FanRenderer.State> {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final int AIRFLOW_COLOR = ARGB.colorFromFloat(0.5f, 0f, 1f, 1f);

    private final BlockModelResolver blockModelResolver;

    public FanRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FanBlockEntity fan, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(fan, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = fan.getBlockState();
        state.facing = blockState.getValue(FanBlock.FACING);
        state.angle = fan.getSpin(partialTicks);
        state.airflow = Config.SHOW_FAN_AIRFLOW.getAsBoolean() ? fan.getAirflowBox() : null;
        blockModelResolver.update(state.propeller,
                BMFBlocks.FAN_PROPELLER.get().defaultBlockState().setValue(FanPropellerBlock.FACING, state.facing), DISPLAY_CONTEXT);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        switch (state.facing) {
            case EAST -> {
                poseStack.translate(13 / 16f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.XP.rotation(-state.angle));
            }
            case SOUTH -> {
                poseStack.translate(0.5f, 0.5f, 13 / 16f);
                poseStack.mulPose(Axis.ZP.rotation(-state.angle));
            }
            case WEST -> {
                poseStack.translate(2 / 16f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.XP.rotation(state.angle));
            }
            default -> {
                poseStack.translate(0.5f, 0.5f, 2 / 16f);
                poseStack.mulPose(Axis.ZP.rotation(state.angle));
            }
        }
        state.propeller.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        if (state.airflow != null) {
            Gizmos.cuboid(state.airflow, GizmoStyle.stroke(AIRFLOW_COLOR));
        }
    }

    // Keep rendering while only the airflow is on screen
    @Override
    public AABB getRenderBoundingBox(FanBlockEntity fan) {
        AABB block = new AABB(fan.getBlockPos());
        AABB airflow = fan.getAirflowBox();
        return airflow != null ? block.minmax(airflow) : block;
    }

    public static class State extends BlockEntityRenderState {
        final BlockModelRenderState propeller = new BlockModelRenderState();
        Direction facing = Direction.NORTH;
        float angle;
        @Nullable AABB airflow;
    }
}
